<?php
 session_start();
 require_once('conf/command.php');
 require_once('../conf/conf.php');
 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
 header("Last-Modified: ".gmdate("D, d M Y H:i:s")." GMT");
 header("Cache-Control: no-store, no-cache, must-revalidate");
 header("Cache-Control: post-check=0, pre-check=0", false);
 header("Pragma: no-cache");
?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <!-- Wajib untuk tablet/HP: tanpa ini canvas tanda tangan ikut ter-zoom
         dan koordinat sentuhan meleset dari goresan yang terlihat. -->
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <title><?php title();?></title>
    <link href="css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="js/jquery.min.js"></script>
</head>
<body>
    <div id="mainContent">
        <?php actionPages();?>
    </div>
</body>
</html>
