<?php
/**
 * vclaim_helper.php
 * Fungsi-fungsi helper untuk bridging BPJS VClaim.
 * Gunakan guard agar tidak terjadi redeclaration jika di-include lebih dari sekali.
 */

if (!defined('VCLAIM_HELPER_LOADED')) {
    define('VCLAIM_HELPER_LOADED', true);

    require_once __DIR__ . '/vendor/autoload.php';
    use LZCompressor\LZString;

    function stringEncrypt($key, $data) {
        $encrypt_method = 'AES-256-CBC';
        $key_hash       = hex2bin(hash('sha256', $key));
        $iv             = substr($key_hash, 0, 16);
        return base64_encode(openssl_encrypt($data, $encrypt_method, $key_hash, OPENSSL_RAW_DATA, $iv));
    }

    function stringDecrypt($key, $string) {
        $encrypt_method = 'AES-256-CBC';
        $key_hash       = hex2bin(hash('sha256', $key));
        $iv             = substr($key_hash, 0, 16);
        return openssl_decrypt(base64_decode($string), $encrypt_method, $key_hash, OPENSSL_RAW_DATA, $iv);
    }

    function compress($string) {
        return LZString::compressToEncodedURIComponent($string);
    }

    function decompress($string) {
        return LZString::decompressFromEncodedURIComponent($string);
    }

    /**
     * Fungsi umum untuk memanggil endpoint VClaim.
     * Ambil $cons_id, $secret_key, $user_key, $base_url dari setting.
     */
    function callVClaim($endpoint, $method = 'GET', $data = null) {
        global $cons_id, $secret_key, $user_key, $base_url;

        date_default_timezone_set('UTC');
        $tStamp    = strval(time());
        date_default_timezone_set('Asia/Jakarta');

        $signature = base64_encode(hash_hmac('sha256', $cons_id . "&" . $tStamp, $secret_key, true));
        $key       = $cons_id . $secret_key . $tStamp;

        $headers = [
            "X-cons-id: $cons_id",
            "X-timestamp: $tStamp",
            "X-signature: $signature",
            "user_key: $user_key",
        ];

        $ch = curl_init($base_url . $endpoint);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_TIMEOUT, 20);

        if ($method === 'POST' && $data !== null) {
            $compressed = compress(json_encode($data));
            $encrypted  = stringEncrypt($key, $compressed);
            $headers[]  = "Content-Type: application/x-www-form-urlencoded";
            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, "request=" . urlencode($encrypted));
        }

        $response = curl_exec($ch);
        $curlErr  = curl_error($ch);
        curl_close($ch);

        if ($curlErr) {
            return ['metaData' => ['code' => 0, 'message' => "cURL error: $curlErr"]];
        }

        $result = json_decode($response, true);
        if (isset($result['response']) && is_string($result['response']) && !empty($result['response'])) {
            $decrypted          = stringDecrypt($key, $result['response']);
            $decompressed       = decompress($decrypted);
            $result['response'] = json_decode($decompressed, true);
        }

        return $result;
    }
}
?>
