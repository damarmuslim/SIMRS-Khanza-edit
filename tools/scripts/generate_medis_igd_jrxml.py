# -*- coding: utf-8 -*-
"""
Generator untuk report/rptCetakPenilaianAwalMedisIGD.jrxml
Format Standar Desain Resmi RSUD Demang Sepulau Raya (RM 03-2)
Desain Mengalir Rapat (Solid Navy Slate Design System):
- Border solid, terpadu, kokoh (lineColor="#334155", pen="0.75") seragam dengan RM 03-4
- Section header seragam: backcolor="#EBF3FA", borderColor="#334155", title bold 8pt #0F172A, subtitle right bold 7.5pt #475569
- Kop RS terpadu satu kotak dengan divider vertikal di x=320 & badge No. RM pill hitam #0F172A
- Dynamic mini-header otomatis muncul pada halaman 2 dan seterusnya ($V{PAGE_NUMBER} > 1)
- Checkbox rapi dengan vector checkmark tebal warna putih (Active Navy #0F172A / Danger Red #991B1B)
- Literal-first comparison (100% null-safe tanpa NPE)
- Tabel diagnostik dan terapi yang rapi dan selaras dengan rumah sakit pemerintah
"""

import os
import sys

xml_header = """<?xml version="1.0" encoding="UTF-8"?>
<!-- Created for RSUD Demang Sepulau Raya - Format Redesain Standar Navy Slate RM 03-2 -->
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd" name="rptCetakPenilaianAwalMedisIGD" language="groovy" pageWidth="609" pageHeight="935" columnWidth="569" leftMargin="20" rightMargin="20" topMargin="20" bottomMargin="20" uuid="7d2a8b9c-e3f4-4a5b-9c1d-8e7f6a5b4c3d">
	<property name="ireport.zoom" value="1.5"/>
	<property name="ireport.x" value="0"/>
	<property name="ireport.y" value="0"/>
	<parameter name="namars" class="java.lang.String"/>
	<parameter name="alamatrs" class="java.lang.String"/>
	<parameter name="kotars" class="java.lang.String"/>
	<parameter name="propinsirs" class="java.lang.String"/>
	<parameter name="kontakrs" class="java.lang.String"/>
	<parameter name="emailrs" class="java.lang.String"/>
	<parameter name="logo" class="java.io.InputStream"/>
	<parameter name="lokalis" class="java.io.InputStream"/>
	<parameter name="segitiga_pat" class="java.io.InputStream"/>
	<parameter name="finger" class="java.lang.String"/>
	<queryString language="SQL">
		<![CDATA[select penilaian_medis_igd.*, reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.tgl_lahir,
if(pasien.jk='L','Laki-Laki','Perempuan') as jk, dokter.nm_dokter, coalesce(pegawai_dokter.nik,dokter.kd_dokter) as nip_dokter, dokter.no_ijn_praktek,
TIMESTAMPDIFF(YEAR,pasien.tgl_lahir,CURDATE()) as umur, reg_periksa.tgl_registrasi, reg_periksa.jam_reg,
konsul_dokter.nm_dokter as konsul_nm_dokter, poliklinik.nm_poli as konsul_nm_poli
from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
inner join penilaian_medis_igd on reg_periksa.no_rawat=penilaian_medis_igd.no_rawat
inner join dokter on penilaian_medis_igd.kd_dokter=dokter.kd_dokter
left join pegawai pegawai_dokter on pegawai_dokter.nik=dokter.kd_dokter
left join dokter konsul_dokter on konsul_dokter.kd_dokter=penilaian_medis_igd.konsul_kd_dokter
left join poliklinik on poliklinik.kd_poli=penilaian_medis_igd.konsul_kd_poli]]>
	</queryString>
	<field name="no_rawat" class="java.lang.String"/>
	<field name="no_rkm_medis" class="java.lang.String"/>
	<field name="nm_pasien" class="java.lang.String"/>
	<field name="jk" class="java.lang.String"/>
	<field name="tgl_lahir" class="java.sql.Date"/>
	<field name="umur" class="java.lang.Integer"/>
	<field name="tgl_registrasi" class="java.sql.Date"/>
	<field name="jam_reg" class="java.sql.Time"/>
	<field name="tanggal" class="java.sql.Timestamp"/>
	<field name="kd_dokter" class="java.lang.String"/>
	<field name="nm_dokter" class="java.lang.String"/>
	<field name="nip_dokter" class="java.lang.String"/>
	<field name="no_ijn_praktek" class="java.lang.String"/>
	<field name="anamnesis" class="java.lang.String"/>
	<field name="hubungan" class="java.lang.String"/>
	<field name="keluhan_utama" class="java.lang.String"/>
	<field name="rps" class="java.lang.String"/>
	<field name="rpd" class="java.lang.String"/>
	<field name="rpk" class="java.lang.String"/>
	<field name="rpo" class="java.lang.String"/>
	<field name="alergi" class="java.lang.String"/>
	<field name="keadaan" class="java.lang.String"/>
	<field name="kesadaran" class="java.lang.String"/>
	<field name="td" class="java.lang.String"/>
	<field name="nadi" class="java.lang.String"/>
	<field name="rr" class="java.lang.String"/>
	<field name="suhu" class="java.lang.String"/>
	<field name="spo" class="java.lang.String"/>
	<field name="bb" class="java.lang.String"/>
	<field name="tb" class="java.lang.String"/>
	<field name="ket_lokalis" class="java.lang.String"/>
	<field name="ket_fisik" class="java.lang.String"/>
	<field name="ekg" class="java.lang.String"/>
	<field name="rad" class="java.lang.String"/>
	<field name="lab" class="java.lang.String"/>
	<field name="tata" class="java.lang.String"/>
	<field name="pre_cervical_collar" class="java.lang.String"/>
	<field name="pre_defibrilasi" class="java.lang.String"/>
	<field name="pre_vtp" class="java.lang.String"/>
	<field name="pre_balut_bidai" class="java.lang.String"/>
	<field name="pre_ngt" class="java.lang.String"/>
	<field name="pre_rjp" class="java.lang.String"/>
	<field name="pre_intubasi" class="java.lang.String"/>
	<field name="pre_dekompresi_wsd" class="java.lang.String"/>
	<field name="pre_kateter_urin" class="java.lang.String"/>
	<field name="pre_infus" class="java.lang.String"/>
	<field name="pre_tidak_ada" class="java.lang.String"/>
	<field name="pre_obat_ket" class="java.lang.String"/>
	<field name="jn_paten" class="java.lang.String"/>
	<field name="jn_obstruksi_partial" class="java.lang.String"/>
	<field name="jn_stridor" class="java.lang.String"/>
	<field name="jn_snoring" class="java.lang.String"/>
	<field name="jn_gurgling" class="java.lang.String"/>
	<field name="jn_wheezing" class="java.lang.String"/>
	<field name="jn_obstruksi_total" class="java.lang.String"/>
	<field name="jn_trauma_jalan_napas" class="java.lang.String"/>
	<field name="jn_fasial_leher_inhalasi" class="java.lang.String"/>
	<field name="jn_risiko_aspirasi" class="java.lang.String"/>
	<field name="jn_benda_asing" class="java.lang.String"/>
	<field name="jn_kesimpulan" class="java.lang.String"/>
	<field name="nap_pola" class="java.lang.String"/>
	<field name="nap_reguler" class="java.lang.String"/>
	<field name="nap_gerakan_dada" class="java.lang.String"/>
	<field name="nap_jejas_kanan" class="java.lang.String"/>
	<field name="nap_jejas_kiri" class="java.lang.String"/>
	<field name="nap_tipe_normal" class="java.lang.String"/>
	<field name="nap_tipe_takipneu" class="java.lang.String"/>
	<field name="nap_tipe_kussmaul" class="java.lang.String"/>
	<field name="nap_tipe_hiperventilasi" class="java.lang.String"/>
	<field name="nap_tipe_biot" class="java.lang.String"/>
	<field name="nap_tipe_cheyne_stoke" class="java.lang.String"/>
	<field name="nap_tipe_apneustic" class="java.lang.String"/>
	<field name="nap_tipe_retraktif" class="java.lang.String"/>
	<field name="nap_tipe_flare" class="java.lang.String"/>
	<field name="nap_kesimpulan" class="java.lang.String"/>
	<field name="sir_nadi" class="java.lang.String"/>
	<field name="sir_nadi_kekuatan" class="java.lang.String"/>
	<field name="sir_kulit_warna" class="java.lang.String"/>
	<field name="sir_kulit_kondisi" class="java.lang.String"/>
	<field name="sir_berkeringat" class="java.lang.String"/>
	<field name="sir_akral" class="java.lang.String"/>
	<field name="sir_akral_kondisi" class="java.lang.String"/>
	<field name="sir_crt" class="java.lang.String"/>
	<field name="sir_kesimpulan" class="java.lang.String"/>
	<field name="kategori_pasien" class="java.lang.String"/>
	<field name="apgar_cukup_bulan" class="java.lang.String"/>
	<field name="apgar_cairan_amnion_jernih" class="java.lang.String"/>
	<field name="apgar_pernapasan_menangis" class="java.lang.String"/>
	<field name="apgar_tonus_otot" class="java.lang.String"/>
	<field name="pat_penampilan" class="java.lang.String"/>
	<field name="pat_usaha_napas" class="java.lang.String"/>
	<field name="pat_perfusi_kulit" class="java.lang.String"/>
	<field name="gcs_e" class="java.lang.String"/>
	<field name="gcs_v" class="java.lang.String"/>
	<field name="gcs_m" class="java.lang.String"/>
	<field name="pupil" class="java.lang.String"/>
	<field name="diameter_pupil_kiri" class="java.lang.String"/>
	<field name="diameter_pupil_kanan" class="java.lang.String"/>
	<field name="refleks_cahaya_kiri" class="java.lang.String"/>
	<field name="refleks_cahaya_kanan" class="java.lang.String"/>
	<field name="meningeal_signs" class="java.lang.String"/>
	<field name="lateralisasi" class="java.lang.String"/>
	<field name="eks_deformitas" class="java.lang.String"/>
	<field name="eks_ekskoriasi" class="java.lang.String"/>
	<field name="eks_contusio" class="java.lang.String"/>
	<field name="eks_abrasi" class="java.lang.String"/>
	<field name="eks_penetrasi" class="java.lang.String"/>
	<field name="eks_burn" class="java.lang.String"/>
	<field name="eks_tenderness" class="java.lang.String"/>
	<field name="eks_laserasi" class="java.lang.String"/>
	<field name="eks_swelling" class="java.lang.String"/>
	<field name="eks_tidak_tampak_jelas" class="java.lang.String"/>
	<field name="konjungtiva" class="java.lang.String"/>
	<field name="sklera" class="java.lang.String"/>
	<field name="bibir_lidah" class="java.lang.String"/>
	<field name="mukosa_mulut" class="java.lang.String"/>
	<field name="deviasi_trakea" class="java.lang.String"/>
	<field name="jvp" class="java.lang.String"/>
	<field name="lnn" class="java.lang.String"/>
	<field name="tiroid" class="java.lang.String"/>
	<field name="jantung" class="java.lang.String"/>
	<field name="paru" class="java.lang.String"/>
	<field name="punggung_pinggang" class="java.lang.String"/>
	<field name="ekstremitas_pelvis" class="java.lang.String"/>
	<field name="pemeriksaan_lainnya" class="java.lang.String"/>
	<field name="diagnosa_kerja_cek" class="java.lang.String"/>
	<field name="diagnosa_kerja" class="java.lang.String"/>
	<field name="diagnosa_banding_cek" class="java.lang.String"/>
	<field name="diagnosa_banding" class="java.lang.String"/>
	<field name="konsul_nm_dokter" class="java.lang.String"/>
	<field name="konsul_nm_poli" class="java.lang.String"/>
	<field name="edukasi_diberikan" class="java.lang.String"/>
	<field name="edukasi_tidak_bisa" class="java.lang.String"/>
	<field name="edukasi_tidak_bisa_ket" class="java.lang.String"/>
	<field name="kondisi_pulang" class="java.lang.String"/>
	<field name="kondisi_pulang_jam" class="java.lang.String"/>
	<field name="td_akhir" class="java.lang.String"/>
	<field name="nadi_akhir" class="java.lang.String"/>
	<field name="rr_akhir" class="java.lang.String"/>
	<field name="suhu_akhir" class="java.lang.String"/>
"""

# ==================== PRIMITIVE HELPERS ====================

def rect(x, y, w, h, pen="0.5", backcolor=None, linecolor="#334155", radius=None, p_when=None, stretch_type=None):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    b_attr = f' backcolor="{backcolor}"' if backcolor else ''
    mode = ' mode="Opaque"' if backcolor else ' mode="Transparent"'
    l_attr = f' lineColor="{linecolor}"' if linecolor else ''
    r_attr = f' radius="{radius}"' if radius is not None else ''
    s_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    return f'''\t\t\t<rectangle{r_attr}>
\t\t\t\t<reportElement positionType="Float"{s_attr}{mode} x="{x}" y="{y}" width="{w}" height="{h}"{b_attr}>{pw_xml}</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="{pen}"{l_attr}/></graphicElement>
\t\t\t</rectangle>\n'''

def line(x, y, w, h, pen="0.5", linecolor="#334155", direction=None, p_when=None, stretch_type=None):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    l_attr = f' lineColor="{linecolor}"' if linecolor else ''
    d_attr = f' direction="{direction}"' if direction else ''
    s_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    return f'''\t\t\t<line{d_attr}>
\t\t\t\t<reportElement positionType="Float"{s_attr} x="{x}" y="{y}" width="{w}" height="{h}">{pw_xml}</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="{pen}"{l_attr}/></graphicElement>
\t\t\t</line>\n'''

def text(x, y, w, h, val, size=7.5, bold=False, italic=False, underline=False, align="Left", vAlign="Middle", color="#0F172A", p_when=None, stretch_type=None):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    sz = int(round(float(size)))
    b_attr = ' isBold="true"' if bold else ''
    i_attr = ' isItalic="true"' if italic else ''
    u_attr = ' isUnderline="true"' if underline else ''
    c_attr = f' forecolor="{color}"' if color else ''
    s_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    return f'''\t\t\t<staticText>
\t\t\t\t<reportElement positionType="Float"{s_attr} x="{x}" y="{y}" width="{w}" height="{h}"{c_attr}>{pw_xml}</reportElement>
\t\t\t\t<textElement textAlignment="{align}" verticalAlignment="{vAlign}">
\t\t\t\t\t<font fontName="Tahoma" size="{sz}"{b_attr}{i_attr}{u_attr}/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<text><![CDATA[{val}]]></text>
\t\t\t</staticText>\n'''

def field(x, y, w, h, expr, size=7.5, bold=False, italic=False, underline=False, align="Left", vAlign="Middle", color="#0F172A", stretch=False, p_when=None, stretch_type=None):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    sz = int(round(float(size)))
    b_attr = ' isBold="true"' if bold else ''
    i_attr = ' isItalic="true"' if italic else ''
    u_attr = ' isUnderline="true"' if underline else ''
    c_attr = f' forecolor="{color}"' if color else ''
    s_attr = ' isStretchWithOverflow="true"' if stretch else ''
    st_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    return f'''\t\t\t<textField{s_attr}>
\t\t\t\t<reportElement positionType="Float"{st_attr} x="{x}" y="{y}" width="{w}" height="{h}"{c_attr}>{pw_xml}</reportElement>
\t\t\t\t<textElement textAlignment="{align}" verticalAlignment="{vAlign}">
\t\t\t\t\t<font fontName="Tahoma" size="{sz}"{b_attr}{i_attr}{u_attr}/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<textFieldExpression><![CDATA[{expr}]]></textFieldExpression>
\t\t\t</textField>\n'''

def cell_text(x, y, w, h, val, size=7.5, bold=False, italic=False, align="Left", vAlign="Top", color="#0F172A", 
              backcolor=None, top_pen="0.5", left_pen="0.5", bottom_pen="0.5", right_pen="0.5", linecolor="#334155",
              top_pad=4, left_pad=6, bottom_pad=4, right_pad=6, stretch_type=None, p_when=None):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    sz = int(round(float(size)))
    b_attr = ' isBold="true"' if bold else ''
    i_attr = ' isItalic="true"' if italic else ''
    c_attr = f' forecolor="{color}"' if color else ''
    st_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    mode_attr = f' mode="Opaque" backcolor="{backcolor}"' if backcolor else ' mode="Transparent"'
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    
    box_xml = f'''\t\t\t\t<box topPadding="{top_pad}" leftPadding="{left_pad}" bottomPadding="{bottom_pad}" rightPadding="{right_pad}">
\t\t\t\t\t<topPen lineWidth="{top_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<leftPen lineWidth="{left_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<bottomPen lineWidth="{bottom_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<rightPen lineWidth="{right_pen}" lineColor="{linecolor}"/>
\t\t\t\t</box>\n'''

    return f'''\t\t\t<staticText>
\t\t\t\t<reportElement positionType="Float"{st_attr}{mode_attr} x="{x}" y="{y}" width="{w}" height="{h}"{c_attr}>{pw_xml}</reportElement>
{box_xml}\t\t\t\t<textElement textAlignment="{align}" verticalAlignment="{vAlign}">
\t\t\t\t\t<font fontName="Tahoma" size="{sz}"{b_attr}{i_attr}/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<text><![CDATA[{val}]]></text>
\t\t\t</staticText>\n'''

def cell_field(x, y, w, h, expr, size=7.5, bold=False, italic=False, align="Left", vAlign="Top", color="#0F172A", 
               stretch=True, stretch_type=None, backcolor=None,
               top_pen="0.5", left_pen="0.5", bottom_pen="0.5", right_pen="0.5", linecolor="#334155",
               top_pad=4, left_pad=6, bottom_pad=4, right_pad=6, p_when=None, line_spacing="Single"):
    x, y, w, h = int(round(float(x))), int(round(float(y))), int(round(float(w))), int(round(float(h)))
    sz = int(round(float(size)))
    b_attr = ' isBold="true"' if bold else ''
    i_attr = ' isItalic="true"' if italic else ''
    c_attr = f' forecolor="{color}"' if color else ''
    s_attr = ' isStretchWithOverflow="true"' if stretch else ''
    st_attr = f' stretchType="{stretch_type}"' if stretch_type else ''
    mode_attr = f' mode="Opaque" backcolor="{backcolor}"' if backcolor else ' mode="Transparent"'
    pw_xml = f'\n\t\t\t\t<printWhenExpression><![CDATA[{p_when}]]></printWhenExpression>' if p_when else ''
    
    box_xml = f'''\t\t\t\t<box topPadding="{top_pad}" leftPadding="{left_pad}" bottomPadding="{bottom_pad}" rightPadding="{right_pad}">
\t\t\t\t\t<topPen lineWidth="{top_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<leftPen lineWidth="{left_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<bottomPen lineWidth="{bottom_pen}" lineColor="{linecolor}"/>
\t\t\t\t\t<rightPen lineWidth="{right_pen}" lineColor="{linecolor}"/>
\t\t\t\t</box>\n'''

    return f'''\t\t\t<textField{s_attr}>
\t\t\t\t<reportElement positionType="Float"{st_attr}{mode_attr} x="{x}" y="{y}" width="{w}" height="{h}"{c_attr}>{pw_xml}</reportElement>
{box_xml}\t\t\t\t<textElement textAlignment="{align}" verticalAlignment="{vAlign}">
\t\t\t\t\t<font fontName="Tahoma" size="{sz}"{b_attr}{i_attr}/>
\t\t\t\t\t<paragraph lineSpacing="{line_spacing}"/>
\t\t\t\t</textElement>
\t\t\t\t<textFieldExpression><![CDATA[{expr}]]></textFieldExpression>
\t\t\t</textField>\n'''

# ==================== COMPOUND HELPERS ====================

def section_bar(y, title, subtitle="", badge="RM 03-2", p_when=None):
    """
    Standard Navy Slate Section Bar (RSUD Demang Sepulau Raya)
    - Full width: 569px
    - Background: Soft navy-slate #EBF3FA
    - Border: Solid #334155 pen="0.5"
    - Title: Bold 8pt #0F172A
    - Subtitle: Right-aligned bold 7.5pt #475569
    """
    out = rect(0, y, 569, 15, pen="0.5", backcolor="#EBF3FA", linecolor="#334155", p_when=p_when)
    if subtitle:
        out += text(8, y + 1, 372, 13, title, size=8, bold=True, color="#0F172A", p_when=p_when)
        if "$F{" in subtitle or "$P{" in subtitle or "$V{" in subtitle:
            out += field(380, y + 1, 181, 13, subtitle, size=7.5, bold=True, align="Right", color="#475569", p_when=p_when)
        else:
            out += text(380, y + 1, 181, 13, subtitle, size=7.5, bold=True, align="Right", color="#475569", p_when=p_when)
    else:
        out += text(8, y + 1, 553, 13, title, size=8, bold=True, color="#0F172A", p_when=p_when)
    return out

def cb(x, y, w, h, label, field_expr, match_val="Ya", is_danger=False):
    """
    Standard Navy Slate Checkbox:
    - Active: Solid box #0F172A (atau Danger Red #991B1B) dengan vector checkmark putih tebal 1.2pt
    - Inactive: Transparent box dengan pen="0.6" lineColor="#475569"
    - 100% Null-safe: literal-first comparison '"{match_val}".equals({field_expr})'
    """
    box_color = "#991B1B" if is_danger else "#0F172A"
    chk_expr = f'"{match_val}".equals({field_expr})'
    unchk_expr = f'!{chk_expr}'

    out = ""
    # 1. Checked box (Active)
    out += f'''\t\t\t<rectangle radius="1">
\t\t\t\t<reportElement positionType="Float" mode="Opaque" x="{x}" y="{y+1}" width="8" height="8" backcolor="{box_color}">
\t\t\t\t\t<printWhenExpression><![CDATA[{chk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="0.0"/></graphicElement>
\t\t\t</rectangle>\n'''
    out += f'''\t\t\t<line>
\t\t\t\t<reportElement positionType="Float" x="{x+1}" y="{y+5}" width="2" height="2">
\t\t\t\t\t<printWhenExpression><![CDATA[{chk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="1.2" lineColor="#FFFFFF"/></graphicElement>
\t\t\t</line>\n'''
    out += f'''\t\t\t<line direction="BottomUp">
\t\t\t\t<reportElement positionType="Float" x="{x+3}" y="{y+2}" width="3" height="5">
\t\t\t\t\t<printWhenExpression><![CDATA[{chk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="1.2" lineColor="#FFFFFF"/></graphicElement>
\t\t\t</line>\n'''

    # 2. Unchecked box (Inactive)
    out += f'''\t\t\t<rectangle radius="1">
\t\t\t\t<reportElement positionType="Float" mode="Transparent" x="{x}" y="{y+1}" width="8" height="8">
\t\t\t\t\t<printWhenExpression><![CDATA[{unchk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<graphicElement><pen lineWidth="0.6" lineColor="#475569"/></graphicElement>
\t\t\t</rectangle>\n'''

    # 3. Label: Bold jika aktif, normal jika tidak aktif
    out += f'''\t\t\t<textField>
\t\t\t\t<reportElement positionType="Float" x="{x+12}" y="{y}" width="{w-12}" height="{h}">
\t\t\t\t\t<printWhenExpression><![CDATA[{chk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<textElement textAlignment="Left" verticalAlignment="Middle">
\t\t\t\t\t<font fontName="Tahoma" size="7" isBold="true"/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<textFieldExpression><![CDATA["{label}"]]></textFieldExpression>
\t\t\t</textField>\n'''
    out += f'''\t\t\t<textField>
\t\t\t\t<reportElement positionType="Float" x="{x+12}" y="{y}" width="{w-12}" height="{h}" forecolor="#334155">
\t\t\t\t\t<printWhenExpression><![CDATA[{unchk_expr}]]></printWhenExpression>
\t\t\t\t</reportElement>
\t\t\t\t<textElement textAlignment="Left" verticalAlignment="Middle">
\t\t\t\t\t<font fontName="Tahoma" size="7"/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<textFieldExpression><![CDATA["{label}"]]></textFieldExpression>
\t\t\t</textField>\n'''
    return out

def ttv_card(x, y, w, h, label, val_expr):
    out = rect(x, y, w, h, pen="0.5", backcolor="#FFFFFF", linecolor="#CBD5E1", radius=3)
    out += text(x, y + 2, w, 10, label, size=6.5, bold=True, align="Center", color="#64748B")
    out += field(x, y + 12, w, 14, val_expr, size=7.5, bold=True, align="Center", color="#0F172A")
    return out

# ==================== PAGE HEADER & FOOTER ====================

def gen_page_header():
    """Mini Header otomatis muncul pada halaman 2 dan seterusnya"""
    out = '\t<pageHeader>\n'
    out += '\t\t<band height="30" splitType="Prevent">\n'
    out += '\t\t\t<printWhenExpression><![CDATA[$V{PAGE_NUMBER} > 1]]></printWhenExpression>\n'
    out += rect(0, 2, 569, 24, pen="0.75", linecolor="#334155", radius=3)
    out += text(8, 8, 42, 12, "No. RM:", size=7.5, bold=True)
    out += rect(52, 7, 48, 14, pen="0.0", backcolor="#0F172A", radius=2)
    out += field(52, 7, 48, 14, '$F{no_rkm_medis}', size=8, bold=True, align="Center", color="#FFFFFF")
    info_expr = '"|  Nama: " + ($F{nm_pasien}!=null?$F{nm_pasien}:"—") + ($F{jk}!=null ? " ("+$F{jk}+")" : "") + "   |   Lahir: " + ($F{tgl_lahir}==null?"-":new java.text.SimpleDateFormat("dd-MM-yyyy").format($F{tgl_lahir}))'
    out += field(105, 8, 395, 12, info_expr, size=7.5, bold=True, align="Left", color="#0F172A")
    out += rect(504, 7, 58, 14, pen="0.75", linecolor="#334155", radius=2)
    out += text(504, 7, 58, 14, "RM 03-2", size=8, bold=True, align="Center", color="#0F172A")
    out += '\t\t</band>\n'
    out += '\t</pageHeader>\n'
    return out

def gen_page_footer():
    """Footer dinamis dengan nomor halaman otomatis (Halaman X dari Y)"""
    out = '\t<pageFooter>\n'
    out += '\t\t<band height="18" splitType="Prevent">\n'
    out += line(0, 2, 569, 1, pen="0.5", linecolor="#CBD5E1")
    footer_text = "RM 03-2 • Pengkajian Awal Medis Gawat Darurat — RSUD Demang Sepulau Raya (SIMRS Khanza)"
    out += text(0, 5, 410, 12, footer_text, size=7, bold=False, color="#64748B")
    out += field(410, 5, 115, 12, '"Halaman " + $V{PAGE_NUMBER} + " dari "', size=7, bold=True, align="Right", color="#64748B")
    out += f'''\t\t\t<textField evaluationTime="Report">
\t\t\t\t<reportElement positionType="Float" x="527" y="5" width="42" height="12" forecolor="#64748B"/>
\t\t\t\t<textElement textAlignment="Left" verticalAlignment="Middle">
\t\t\t\t\t<font fontName="Tahoma" size="7" isBold="true"/>
\t\t\t\t</textElement>
\t\t\t\t<textFieldExpression><![CDATA["" + $V{{PAGE_NUMBER}}]]></textFieldExpression>
\t\t\t</textField>\n'''
    out += '\t\t</band>\n'
    out += '\t</pageFooter>\n'
    return out

# ==================== MODULAR DETAIL BANDS ====================

# ==============================================================================
# BAND 0: KOP RS, IDENTITAS PASIEN, JUDUL FORMULIR RM 03-2
# ==============================================================================
def gen_band_header():
    b = '\t\t<band height="96" splitType="Prevent">\n'
    
    # 1. KOP RS BOX SATU KESATUAN (569 x 58, divider vertikal di x=320)
    b += rect(0, 0, 569, 58, pen="0.75", linecolor="#334155", radius=3)
    b += line(320, 0, 1, 58, pen="0.75", linecolor="#334155")

    # KOP Kiri (Logo & Identitas RS)
    b += '''\t\t\t<image scaleImage="FillFrame" isUsingCache="true" onErrorType="Blank">
\t\t\t\t<reportElement positionType="Float" x="8" y="8" width="42" height="42"/>
\t\t\t\t<imageExpression><![CDATA[$P{logo}]]></imageExpression>
\t\t\t</image>\n'''
    b += field(56, 6, 258, 14, '$P{namars}', size=10, bold=True, align="Left", color="#0F172A")
    b += field(56, 20, 258, 18, '($P{alamatrs}!=null?$P{alamatrs}:"")+(($P{kotars}!=null&&!$P{kotars}.isEmpty())?", "+$P{kotars}:"")', size=7, align="Left", color="#334155")
    b += field(56, 38, 258, 14, '($P{kontakrs}!=null?$P{kontakrs}:"")+" • E-mail: "+($P{emailrs}!=null?$P{emailrs}:"")', size=6.5, align="Left", color="#334155")

    # KOP Kanan (Stiker Identitas Pasien)
    b += text(328, 5, 75, 12, "No. Rekam Medis", size=7, bold=True, color="#1E293B")
    b += text(405, 5, 5, 12, ":", size=7, bold=True)
    b += rect(414, 4, 52, 13, pen="0.0", backcolor="#0F172A", radius=2)
    b += field(414, 4, 52, 13, '$F{no_rkm_medis}', size=8, bold=True, align="Center", color="#FFFFFF")

    b += text(328, 18, 75, 11, "Nama Pasien", size=7, bold=True, color="#1E293B")
    b += text(405, 18, 5, 11, ":", size=7, bold=True)
    b += field(414, 18, 150, 11, '($F{nm_pasien}!=null?$F{nm_pasien}:"—")+($F{jk}==null?"":" ("+$F{jk}+")")', size=7.5, bold=True, color="#0F172A")

    b += text(328, 30, 75, 11, "Tgl. Lahir / Umur", size=7, bold=True, color="#1E293B")
    b += text(405, 30, 5, 11, ":", size=7, bold=True)
    b += field(414, 30, 150, 11, '($F{tgl_lahir}==null?"-":new java.text.SimpleDateFormat("dd-MM-yyyy").format($F{tgl_lahir}))+($F{umur}==null?"":" / "+$F{umur}+" th")', size=7.5, bold=True, color="#0F172A")

    b += text(328, 42, 75, 11, "Tgl. Masuk RS", size=7, bold=True, color="#1E293B")
    b += text(405, 42, 5, 11, ":", size=7, bold=True)
    b += field(414, 42, 150, 11, '($F{tgl_registrasi}==null?"-":new java.text.SimpleDateFormat("dd-MM-yyyy").format($F{tgl_registrasi}))+" • "+($F{jam_reg}==null?"":new java.text.SimpleDateFormat("HH:mm").format($F{jam_reg}))+" WIB"', size=7.5, bold=True, color="#0F172A")

    # 2. JUDUL FORMULIR
    b += rect(0, 62, 569, 30, pen="0.75", linecolor="#334155", radius=2)
    b += text(8, 64, 480, 13, "PENGKAJIAN AWAL MEDIS GAWAT DARURAT", size=9, bold=True, color="#0F172A")
    b += rect(504, 64, 58, 13, pen="0.75", linecolor="#334155", radius=2)
    b += text(504, 64, 58, 13, "RM 03-2", size=8, bold=True, align="Center", color="#0F172A")
    b += line(0, 78, 569, 1, pen="0.5", linecolor="#CBD5E1")
    
    sub_kiri = '"Instalasi Gawat Darurat (IGD) • Kategori: " + ($F{kategori_pasien}!=null?$F{kategori_pasien}:"Umum")'
    b += field(8, 79, 320, 12, sub_kiri, size=7.5, bold=True, color="#0F172A")
    sub_kanan = '"Mulai Penanganan: " + ($F{tanggal}==null?"-":new java.text.SimpleDateFormat("dd/MM/yyyy • HH:mm:ss").format($F{tanggal})) + " WIB"'
    b += field(330, 79, 231, 12, sub_kanan, size=7, bold=True, align="Right", color="#334155")

    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 1: INTERVENSI PRE HOSPITAL
# ==============================================================================
def gen_band_prehospital():
    b = '\t\t<band height="64" splitType="Prevent">\n'
    b += section_bar(2, "INTERVENSI PRE HOSPITAL", subtitle="TINDAKAN PRA-RUMAH SAKIT")
    
    b += rect(0, 18, 569, 43, pen="0.75", linecolor="#334155")
    # Row 1 (6 items)
    b += cb(6, 21, 85, 11, "Cervical Collar", "$F{pre_cervical_collar}")
    b += cb(96, 21, 80, 11, "Defibrilasi", "$F{pre_defibrilasi}")
    b += cb(182, 21, 60, 11, "VTP", "$F{pre_vtp}")
    b += cb(252, 21, 80, 11, "Balut Bidai", "$F{pre_balut_bidai}")
    b += cb(342, 21, 60, 11, "NGT", "$F{pre_ngt}")
    b += cb(412, 21, 60, 11, "RJP", "$F{pre_rjp}")
    # Row 2 (5 items)
    b += cb(6, 33, 85, 11, "Intubasi", "$F{pre_intubasi}")
    b += cb(96, 33, 85, 11, "Dekompresi/WSD", "$F{pre_dekompresi_wsd}")
    b += cb(182, 33, 80, 11, "Kateter Urin", "$F{pre_kateter_urin}")
    b += cb(272, 33, 65, 11, "Infus", "$F{pre_infus}")
    b += cb(352, 33, 75, 11, "Tidak Ada", "$F{pre_tidak_ada}")
    # Row 3 (Obat / Cairan)
    b += line(4, 46, 561, 1, pen="0.4", linecolor="#E2E8F0")
    b += field(6, 48, 558, 11, '"Obat / Cairan yang Diberikan: " + ($F{pre_obat_ket}==null||$F{pre_obat_ket}.trim().isEmpty()?"-":$F{pre_obat_ket})', size=7, bold=True, color="#0F172A")
    
    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 2: SURVEI PRIMER (PRIMARY SURVEY — A-B-C-D-E)
# ==============================================================================
def gen_band_survei_primer():
    b = '\t\t<band height="268" splitType="Prevent">\n'
    b += section_bar(2, "SURVEI PRIMER (PRIMARY SURVEY — A-B-C-D-E)", subtitle="EVALUASI KEGAWATDARURATAN RESUSITASI")
    
    # --- 3 Kolom Atas (y=18, h=167) ---
    # Kolom 1: JALAN NAPAS (AIRWAY) [A]
    b += rect(0, 18, 187, 167, pen="0.75", linecolor="#334155")
    b += rect(0, 18, 187, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(6, 18, 145, 14, "JALAN NAPAS (AIRWAY)", size=7, bold=True, color="#0F172A")
    b += text(155, 18, 26, 14, "[A]", size=7, bold=True, align="Right", color="#0F172A")
    # Subkolom kiri (x=5)
    b += cb(5, 34, 85, 11, "Paten", "$F{jn_paten}")
    b += cb(5, 47, 85, 11, "Stridor", "$F{jn_stridor}")
    b += cb(5, 60, 85, 11, "Gurgling", "$F{jn_gurgling}")
    b += cb(5, 73, 85, 11, "Obstruksi Total", "$F{jn_obstruksi_total}")
    b += cb(5, 86, 85, 11, "Risiko Aspirasi", "$F{jn_risiko_aspirasi}")
    b += cb(5, 99, 90, 11, "Fasial/Leher/Inhalasi", "$F{jn_fasial_leher_inhalasi}")
    # Subkolom kanan (x=98)
    b += cb(98, 34, 85, 11, "Obstruksi Partial", "$F{jn_obstruksi_partial}")
    b += cb(98, 47, 85, 11, "Snoring", "$F{jn_snoring}")
    b += cb(98, 60, 85, 11, "Wheezing", "$F{jn_wheezing}")
    b += cb(98, 73, 85, 11, "Trauma Jln Napas", "$F{jn_trauma_jalan_napas}")
    b += cb(98, 86, 85, 11, "Benda Asing", "$F{jn_benda_asing}")
    # Kesimpulan Airway
    b += line(0, 166, 187, 1, pen="0.5", linecolor="#CBD5E1")
    b += text(5, 168, 55, 14, "Kesimpulan:", size=7, bold=True)
    b += cb(62, 169, 45, 12, "Aman", "$F{jn_kesimpulan}", match_val="Aman")
    b += cb(108, 169, 76, 12, "Mengancam Jiwa", "$F{jn_kesimpulan}", match_val="Mengancam Jiwa", is_danger=True)

    # Kolom 2: PERNAPASAN (BREATHING) [B]
    b += rect(191, 18, 187, 167, pen="0.75", linecolor="#334155")
    b += rect(191, 18, 187, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(197, 18, 145, 14, "PERNAPASAN (BREATHING)", size=7, bold=True, color="#0F172A")
    b += text(346, 18, 26, 14, "[B]", size=7, bold=True, align="Right", color="#0F172A")
    # Baris 1: Spontan & Tidak Spontan
    b += cb(196, 34, 80, 11, "Spontan", "$F{nap_pola}", match_val="Spontan")
    b += cb(282, 34, 92, 11, "Tidak Spontan", "$F{nap_pola}", match_val="Tidak Spontan")
    # Baris 2: Reguler & Irreguler
    b += cb(196, 47, 80, 11, "Reguler", "$F{nap_reguler}", match_val="Reguler")
    b += cb(282, 47, 92, 11, "Irreguler", "$F{nap_reguler}", match_val="Irreguler")
    # Gerakan Dada
    b += text(196, 59, 178, 10, "Gerakan Dada:", size=6.5, bold=True, color="#475569")
    b += cb(196, 69, 80, 11, "Simetris", "$F{nap_gerakan_dada}", match_val="Simetris")
    b += cb(282, 69, 92, 11, "Asimetris", "$F{nap_gerakan_dada}", match_val="Asimetris")
    b += cb(196, 81, 80, 11, "Jejas Kanan", "$F{nap_jejas_kanan}")
    b += cb(282, 81, 92, 11, "Jejas Kiri", "$F{nap_jejas_kiri}")
    # Tipe Pernapasan
    b += text(196, 93, 178, 10, "Tipe Pernapasan:", size=6.5, bold=True, color="#475569")
    b += cb(196, 103, 80, 10, "Normal", "$F{nap_tipe_normal}")
    b += cb(282, 103, 92, 10, "Takipneu", "$F{nap_tipe_takipneu}")
    b += cb(196, 114, 80, 10, "Kussmaul", "$F{nap_tipe_kussmaul}")
    b += cb(282, 114, 92, 10, "Hiperventilasi", "$F{nap_tipe_hiperventilasi}")
    b += cb(196, 125, 80, 10, "Biot", "$F{nap_tipe_biot}")
    b += cb(282, 125, 92, 10, "Cheyne Stoke", "$F{nap_tipe_cheyne_stoke}")
    b += cb(196, 136, 80, 10, "Apneustic", "$F{nap_tipe_apneustic}")
    b += cb(282, 136, 92, 10, "Retraktif", "$F{nap_tipe_retraktif}")
    b += cb(196, 147, 80, 10, "Flare", "$F{nap_tipe_flare}")
    # Kesimpulan Breathing
    b += line(191, 166, 187, 1, pen="0.5", linecolor="#CBD5E1")
    b += text(196, 168, 55, 14, "Kesimpulan:", size=7, bold=True)
    b += cb(253, 169, 45, 12, "Aman", "$F{nap_kesimpulan}", match_val="Aman")
    b += cb(299, 169, 76, 12, "Mengancam Jiwa", "$F{nap_kesimpulan}", match_val="Mengancam Jiwa", is_danger=True)

    # Kolom 3: SIRKULASI (CIRCULATION) [C]
    b += rect(382, 18, 187, 167, pen="0.75", linecolor="#334155")
    b += rect(382, 18, 187, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(388, 18, 145, 14, "SIRKULASI (CIRCULATION)", size=7, bold=True, color="#0F172A")
    b += text(537, 18, 26, 14, "[C]", size=7, bold=True, align="Right", color="#0F172A")
    # Nadi
    b += text(387, 33, 178, 10, "Nadi:", size=6.5, bold=True, color="#475569")
    b += cb(387, 43, 80, 11, "Reguler", "$F{sir_nadi}", match_val="Reguler")
    b += cb(473, 43, 92, 11, "Irreguler", "$F{sir_nadi}", match_val="Irreguler")
    b += cb(387, 55, 80, 11, "Kuat", "$F{sir_nadi_kekuatan}", match_val="Kuat")
    b += cb(473, 55, 92, 11, "Lemah", "$F{sir_nadi_kekuatan}", match_val="Lemah")
    # Kulit / Mukosa
    b += text(387, 67, 178, 10, "Kulit / Mukosa:", size=6.5, bold=True, color="#475569")
    b += cb(387, 77, 80, 11, "Normal", "$F{sir_kulit_warna}", match_val="Normal")
    b += cb(473, 77, 92, 11, "Pucat", "$F{sir_kulit_warna}", match_val="Pucat")
    b += cb(387, 89, 80, 11, "Jaundice", "$F{sir_kulit_kondisi}", match_val="Jaundice")
    b += cb(473, 89, 92, 11, "Cyanosis", "$F{sir_kulit_kondisi}", match_val="Cyanosis")
    b += cb(387, 101, 90, 11, "Berkeringat", "$F{sir_berkeringat}")
    # Akral & CRT
    b += text(387, 113, 178, 10, "Akral & CRT:", size=6.5, bold=True, color="#475569")
    b += cb(387, 123, 80, 11, "Hangat", "$F{sir_akral}", match_val="Hangat")
    b += cb(473, 123, 92, 11, "Dingin", "$F{sir_akral}", match_val="Dingin")
    b += cb(387, 135, 80, 11, "Kering", "$F{sir_akral_kondisi}", match_val="Kering")
    b += cb(473, 135, 92, 11, "Basah", "$F{sir_akral_kondisi}", match_val="Basah")
    b += cb(387, 147, 80, 11, "CRT < 2 dtk", "$F{sir_crt}", match_val="<2 detik")
    b += cb(473, 147, 92, 11, "CRT > 2 dtk", "$F{sir_crt}", match_val=">2 detik")
    # Kesimpulan Sirkulasi
    b += line(382, 166, 187, 1, pen="0.5", linecolor="#CBD5E1")
    b += text(387, 168, 55, 14, "Kesimpulan:", size=7, bold=True)
    b += cb(444, 169, 45, 12, "Aman", "$F{sir_kesimpulan}", match_val="Aman")
    b += cb(490, 169, 76, 12, "Mengancam Jiwa", "$F{sir_kesimpulan}", match_val="Mengancam Jiwa", is_danger=True)

    # --- 2 Kolom Bawah (y=188, h=78) ---
    # Kolom 1: DISABILITAS (NEUROLOGIS) [D]
    b += rect(0, 188, 282, 78, pen="0.75", linecolor="#334155")
    b += rect(0, 188, 282, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(6, 188, 220, 14, "DISABILITAS (NEUROLOGIS)", size=7, bold=True, color="#0F172A")
    b += text(250, 188, 26, 14, "[D]", size=7, bold=True, align="Right", color="#0F172A")
    # Subkolom kiri (x=5)
    b += field(5, 203, 135, 11, '"Kesadaran: " + ($F{kesadaran}==null||$F{kesadaran}.isEmpty()?"-":$F{kesadaran})', size=7, bold=True)
    b += field(5, 215, 135, 11, '"GCS (E, V, M): " + ($F{gcs_e}.isEmpty()?"-":$F{gcs_e}) + " / " + ($F{gcs_v}.isEmpty()?"-":$F{gcs_v}) + " / " + ($F{gcs_m}.isEmpty()?"-":$F{gcs_m})', size=7, bold=True)
    b += text(5, 227, 30, 11, "Pupil:", size=7, bold=True)
    b += cb(36, 227, 50, 11, "Isokor", "$F{pupil}", match_val="Isokor")
    b += cb(88, 227, 55, 11, "Anisokor", "$F{pupil}", match_val="Anisokor")
    b += field(5, 239, 135, 11, '"Diameter Ki/Ka: " + ($F{diameter_pupil_kiri}.isEmpty()?"-":$F{diameter_pupil_kiri}) + " / " + ($F{diameter_pupil_kanan}.isEmpty()?"-":$F{diameter_pupil_kanan})', size=7, bold=True)
    # Subkolom kanan (x=145)
    b += field(145, 203, 132, 11, '"Reflek Cahaya Kiri: " + ("Positif".equals($F{refleks_cahaya_kiri})?"Positif (+)":"Negatif (-)")', size=7, bold=True)
    b += field(145, 215, 132, 11, '"Reflek Cahaya Kanan: " + ("Positif".equals($F{refleks_cahaya_kanan})?"Positif (+)":"Negatif (-)")', size=7, bold=True)
    b += field(145, 227, 132, 11, '"Meningeal Signs: " + ($F{meningeal_signs}.isEmpty()?"-":$F{meningeal_signs})', size=7, bold=True)
    b += field(145, 239, 132, 11, '"Lateralisasi: " + ($F{lateralisasi}==null||$F{lateralisasi}.isEmpty()?"-":$F{lateralisasi})', size=7, bold=True)

    # Kolom 2: EKSPOSUR (EXPOSURE / TRAUMA) [E]
    b += rect(287, 188, 282, 78, pen="0.75", linecolor="#334155")
    b += rect(287, 188, 282, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(293, 188, 220, 14, "EKSPOSUR (EXPOSURE / TRAUMA)", size=7, bold=True, color="#0F172A")
    b += text(537, 188, 26, 14, "[E]", size=7, bold=True, align="Right", color="#0F172A")
    # Grid Checkbox Trauma
    b += cb(292, 203, 85, 11, "Deformitas", "$F{eks_deformitas}", is_danger=True)
    b += cb(382, 203, 85, 11, "Contusio", "$F{eks_contusio}", is_danger=True)
    b += cb(472, 203, 85, 11, "Abrasi", "$F{eks_abrasi}", is_danger=True)
    b += cb(292, 216, 85, 11, "Penetrasi", "$F{eks_penetrasi}", is_danger=True)
    b += cb(382, 216, 85, 11, "Laserasi", "$F{eks_laserasi}", is_danger=True)
    b += cb(472, 216, 92, 11, "Edema / Swelling", "$F{eks_swelling}", is_danger=True)
    b += cb(292, 229, 85, 11, "Ekskoriasi", "$F{eks_ekskoriasi}", is_danger=True)
    b += cb(382, 229, 95, 11, "Burn (Luka Bakar)", "$F{eks_burn}", is_danger=True)
    b += cb(472, 229, 85, 11, "Tenderness", "$F{eks_tenderness}", is_danger=True)
    b += cb(292, 242, 120, 11, "Tidak Tampak Jelas", "$F{eks_tidak_tampak_jelas}")

    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 3: ANAMNESIS (DINAMIS DENGAN BOX BORDER BERSAMA)
# ==============================================================================
def gen_band_anamnesis():
    b = '\t\t<band height="44" splitType="Stretch">\n'
    subtitle_anamnesis = '("DATA SUBJEKTIF MEDIS • " + ($F{anamnesis}!=null?$F{anamnesis}:"Autoanamnesis") + ($F{hubungan}!=null&&!$F{hubungan}.trim().isEmpty()&&!$F{hubungan}.equals("-")?" ("+$F{hubungan}+")":""))'
    b += section_bar(2, "ANAMNESIS", subtitle=subtitle_anamnesis)
    
    b += cell_text(0, 18, 100, 24, "Anamnesis", size=7.5, bold=True, color="#1E293B", backcolor="#F8FAFC",
                   top_pen="0.75", left_pen="0.75", bottom_pen="0.75", right_pen="0.75", linecolor="#334155",
                   top_pad=6, left_pad=6, bottom_pad=6, right_pad=6, stretch_type="RelativeToTallestObject")
    
    b += cell_field(100, 18, 469, 24, '($F{keluhan_utama}==null||$F{keluhan_utama}.isEmpty()?"-":$F{keluhan_utama})',
                    size=7.5, color="#0F172A", stretch=True, stretch_type="RelativeToTallestObject",
                    top_pen="0.75", left_pen="0.0", bottom_pen="0.75", right_pen="0.75", linecolor="#334155",
                    top_pad=6, left_pad=6, bottom_pad=6, right_pad=6, line_spacing="Single")
    
    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 4: EVALUASI RESUSITASI BAYI BARU LAHIR & SEGITIGA PAT
# (Tampil khusus Kategori Pasien: Bayi Baru Lahir, mengambil 0 space jika bukan Bayi)
# ==============================================================================
def gen_band_bayi_pat():
    cond_bayi = '"Bayi Baru Lahir".equals($F{kategori_pasien})'
    b = f'\t\t<band height="248" splitType="Prevent">\n'
    b += f'\t\t\t<printWhenExpression><![CDATA[{cond_bayi}]]></printWhenExpression>\n'
    
    b += section_bar(2, "EVALUASI RESUSITASI BAYI BARU LAHIR & SEGITIGA PENILAIAN PEDIATRIK (PAT)", subtitle="PEDIATRIC ASSESSMENT TRIANGLE (PAT)")
    # Box Pembungkus Utama (h=227, y=18)
    b += rect(0, 18, 569, 227, pen="0.75", linecolor="#334155")
    # Garis Pembagi Vertikal Kolom
    b += line(284, 18, 1, 227, pen="0.75", linecolor="#334155")

    # --- KOLOM KIRI: Evaluasi Awal Resusitasi (APGAR) ---
    b += rect(0, 18, 284, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(6, 18, 272, 14, "EVALUASI AWAL RESUSITASI (APGAR)", size=7, bold=True, color="#0F172A")
    
    # 4 Kriteria APGAR
    b += text(6, 36, 145, 12, "1. Cukup Bulan (Aterm)?", size=6.5, bold=True)
    b += rect(155, 35, 122, 13, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1")
    b += field(157, 35, 118, 13, '($F{apgar_cukup_bulan}!=null&&!$F{apgar_cukup_bulan}.isEmpty()?$F{apgar_cukup_bulan}:"-")', size=6.5, bold=True, align="Center", color="#0F172A")

    b += text(6, 51, 145, 12, "2. Air Ketuban Jernih?", size=6.5, bold=True)
    b += rect(155, 50, 122, 13, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1")
    b += field(157, 50, 118, 13, '($F{apgar_cairan_amnion_jernih}!=null&&!$F{apgar_cairan_amnion_jernih}.isEmpty()?$F{apgar_cairan_amnion_jernih}:"-")', size=6.5, bold=True, align="Center", color="#0F172A")

    b += text(6, 66, 145, 12, "3. Bernapas / Menangis Kuat?", size=6.5, bold=True)
    b += rect(155, 65, 122, 13, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1")
    b += field(157, 65, 118, 13, '($F{apgar_pernapasan_menangis}!=null&&!$F{apgar_pernapasan_menangis}.isEmpty()?$F{apgar_pernapasan_menangis}:"-")', size=6.5, bold=True, align="Center", color="#0F172A")

    b += text(6, 81, 145, 12, "4. Tonus Otot Baik / Aktif?", size=6.5, bold=True)
    b += rect(155, 80, 122, 13, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1")
    b += field(157, 80, 118, 13, '($F{apgar_tonus_otot}!=null&&!$F{apgar_tonus_otot}.isEmpty()?$F{apgar_tonus_otot}:"-")', size=6.5, bold=True, align="Center", color="#0F172A")

    b += line(4, 98, 276, 1, pen="0.4", linecolor="#E2E8F0")
    
    # Box Panduan Resusitasi (Tetap Hijau untuk Safety Guidelines)
    b += rect(6, 103, 272, 136, pen="0.5", backcolor="#F0FDF4", linecolor="#A7F3D0")
    b += text(10, 105, 264, 12, "PEDOMAN TINDAKAN ALUR RESUSITASI BAYI:", size=7, bold=True, color="#047857")
    pedoman_txt = (
        "1. BILA SEMUA 'YA' (Bayi Bugar / Aterm):\n"
        "   - Berikan perawatan rutin: jaga bayi tetap hangat di dada ibu,\n"
        "     bersihkan jalan napas bila perlu, keringkan, observasi tanda vital.\n\n"
        "2. BILA ADA 'TIDAK' (Bayi Tidak Bugar / Preterm):\n"
        "   - Segera lakukan langkah awal resusitasi dalam 30 detik:\n"
        "     hangatkan (infant warmer), posisikan kepala sedikit ekstensi,\n"
        "     isap lendir mulut lalu hidung, keringkan dan rangsang taktil.\n\n"
        "3. EVALUASI (Frekuensi Jantung & Napas):\n"
        "   - Bila apneu atau denyut < 100 x/m, pasang pulse oximeter dan\n"
        "     mulai ventilasi tekanan positif (VTP) dengan sungkup."
    )
    b += text(10, 118, 264, 118, pedoman_txt, size=6.5, color="#1E293B")

    # --- KOLOM KANAN: Visualisasi Segitiga Penilaian Pediatrik (PAT) ---
    b += rect(284, 18, 285, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(291, 18, 274, 14, "VISUALISASI SEGITIGA PENILAIAN PEDIATRIK (PAT)", size=7, bold=True, color="#0F172A")

    # Gambar Segitiga PAT (Visual Triase Pediatrik Fungsional)
    b += f'''\t\t\t<image scaleImage="RetainShape" hAlign="Center" vAlign="Middle" isUsingCache="true" onErrorType="Blank">
\t\t\t\t<reportElement positionType="Float" x="295" y="36" width="264" height="120"/>
\t\t\t\t<imageExpression><![CDATA[$P{{segitiga_pat}} != null ? $P{{segitiga_pat}} : rekammedis.RMPenilaianAwalMedisIGD.gambarSegitigaPATStream($F{{pat_penampilan}}, $F{{pat_usaha_napas}}, $F{{pat_perfusi_kulit}})]]></imageExpression>
\t\t\t</image>\n'''

    # Rincian 3 Sisi PAT
    b += text(290, 160, 75, 11, "Penampilan", size=6.5, bold=True)
    b += text(365, 160, 5, 11, ":", size=6.5, bold=True)
    b += field(372, 160, 190, 11, '($F{pat_penampilan}!=null?$F{pat_penampilan}:"-")', size=6.5)

    b += text(290, 172, 75, 11, "Usaha Napas", size=6.5, bold=True)
    b += text(365, 172, 5, 11, ":", size=6.5, bold=True)
    b += field(372, 172, 190, 11, '($F{pat_usaha_napas}!=null?$F{pat_usaha_napas}:"-")', size=6.5)

    b += text(290, 184, 75, 11, "Perfusi Kulit", size=6.5, bold=True)
    b += text(365, 184, 5, 11, ":", size=6.5, bold=True)
    b += field(372, 184, 190, 11, '($F{pat_perfusi_kulit}!=null?$F{pat_perfusi_kulit}:"-")', size=6.5)

    # Box Kesan PAT
    b += rect(290, 200, 273, 40, pen="0.5", backcolor="#F0FDF4", linecolor="#A7F3D0")
    kesan_expr = '("KESAN PAT: " + rekammedis.RMPenilaianAwalMedisIGD.getKesanPAT($F{pat_penampilan}, $F{pat_usaha_napas}, $F{pat_perfusi_kulit}))'
    b += field(294, 203, 265, 34, kesan_expr, size=6.5, bold=True, align="Center", color="#065F46")

    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 5: STATUS LOKALIS DAN PEMERIKSAAN FISIK (HEAD-TO-TOE)
# ==============================================================================
def gen_band_lokalis_fisik():
    b = '\t\t<band height="256" splitType="Prevent">\n'
    b += section_bar(2, "STATUS LOKALIS DAN PEMERIKSAAN FISIK", subtitle="PEMETAAN ANATOMI & PEMERIKSAAN SISTEMIK")

    # Kolom Kiri: Peta Anatomi Status Lokalis (w=280)
    b += rect(0, 19, 280, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(6, 19, 175, 14, "PETA ANATOMI STATUS LOKALIS (REGIO 1-12)", size=7, bold=True, color="#0F172A")
    b += text(182, 19, 92, 14, "Area Temuan / Jejas", size=6.5, bold=True, align="Right", color="#B91C1C")
    
    # Outer rect untuk kolom kiri dengan stretchType="RelativeToTallestObject"
    b += rect(0, 33, 280, 218, pen="0.75", linecolor="#334155", stretch_type="RelativeToTallestObject")
    
    # Gambar Lokalis (mengikuti sistem via $P{lokalis})
    b += '''\t\t\t<image scaleImage="RetainShape" hAlign="Center" vAlign="Middle" isUsingCache="true" onErrorType="Blank">
\t\t\t\t<reportElement positionType="Float" x="6" y="37" width="268" height="175"/>
\t\t\t\t<imageExpression><![CDATA[$P{lokalis}]]></imageExpression>
\t\t\t</image>\n'''
    
    # Pill Keterangan Lokalis
    b += rect(6, 217, 268, 28, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1", radius=2)
    b += field(10, 219, 260, 24, '"Keterangan Status Lokalis: " + ($F{ket_lokalis}==null||$F{ket_lokalis}.isEmpty()?"-":$F{ket_lokalis})', size=7, bold=True, color="#0F172A", stretch=True)

    # Kolom Kanan: PEMERIKSAAN FISIK (HEAD-TO-TOE) - Mengembang Dinamis Bersama Box (w=284)
    b += rect(285, 19, 284, 14, pen="0.5", backcolor="#EBF3FA", linecolor="#334155")
    b += text(291, 19, 275, 14, "PEMERIKSAAN FISIK (HEAD-TO-TOE)", size=7.5, bold=True, color="#0F172A")
    
    # Cell Field dengan <box> membungkus teks pemeriksaan fisik seutuhnya
    b += cell_field(285, 33, 284, 218, '($F{ket_fisik}==null||$F{ket_fisik}.trim().isEmpty()?"-":$F{ket_fisik})',
                    size=7.5, color="#0F172A", stretch=True, stretch_type="RelativeToTallestObject",
                    top_pen="0.0", left_pen="0.75", bottom_pen="0.75", right_pen="0.75", linecolor="#334155",
                    top_pad=6, left_pad=6, bottom_pad=6, right_pad=6, line_spacing="Single")

    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 6: PEMERIKSAAN PENUNJANG (TABEL TERPADU: LAB, RADIOLOGI, EKG)
# ==============================================================================
def _row_penunjang(y, label, expr_nama, expr_status, is_last=False):
    bottom_pen = "0.75" if is_last else "0.5"
    r = '\t\t\t<frame>\n'
    r += f'\t\t\t\t<reportElement positionType="Float" x="0" y="{y}" width="569" height="18"/>\n'
    r += f'''\t\t\t\t<box>
\t\t\t\t\t<topPen lineWidth="0.0" lineColor="#334155"/>
\t\t\t\t\t<leftPen lineWidth="0.75" lineColor="#334155"/>
\t\t\t\t\t<bottomPen lineWidth="{bottom_pen}" lineColor="#334155"/>
\t\t\t\t\t<rightPen lineWidth="0.75" lineColor="#334155"/>
\t\t\t\t</box>\n'''
    # Col 1: Jenis Penunjang
    r += f'''\t\t\t\t<textField isStretchWithOverflow="true">
\t\t\t\t\t<reportElement positionType="Float" stretchType="RelativeToTallestObject" mode="Opaque" backcolor="#F8FAFC" x="0" y="0" width="115" height="18" forecolor="#0F172A"/>
\t\t\t\t\t<box topPadding="4" leftPadding="4" bottomPadding="4" rightPadding="4"/>
\t\t\t\t\t<textElement textAlignment="Center" verticalAlignment="Middle">
\t\t\t\t\t\t<font fontName="Tahoma" size="8" isBold="true"/>
\t\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t\t</textElement>
\t\t\t\t\t<textFieldExpression><![CDATA["{label}"]]></textFieldExpression>
\t\t\t\t</textField>\n'''
    # Vertical Line at x=115
    r += f'''\t\t\t\t<line>
\t\t\t\t\t<reportElement positionType="Float" stretchType="RelativeToTallestObject" x="115" y="0" width="1" height="18"/>
\t\t\t\t\t<graphicElement><pen lineWidth="0.5" lineColor="#334155"/></graphicElement>
\t\t\t\t</line>\n'''
    # Col 2: Pemeriksaan / Tindakan
    r += f'''\t\t\t\t<textField isStretchWithOverflow="true">
\t\t\t\t\t<reportElement positionType="Float" stretchType="RelativeToTallestObject" mode="Transparent" x="116" y="0" width="314" height="18" forecolor="#0F172A"/>
\t\t\t\t\t<box topPadding="4" leftPadding="6" bottomPadding="4" rightPadding="6"/>
\t\t\t\t\t<textElement textAlignment="Left" verticalAlignment="Middle">
\t\t\t\t\t\t<font fontName="Tahoma" size="8"/>
\t\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t\t</textElement>
\t\t\t\t\t<textFieldExpression><![CDATA[{expr_nama}]]></textFieldExpression>
\t\t\t\t</textField>\n'''
    # Vertical Line at x=430
    r += f'''\t\t\t\t<line>
\t\t\t\t\t<reportElement positionType="Float" stretchType="RelativeToTallestObject" x="430" y="0" width="1" height="18"/>
\t\t\t\t\t<graphicElement><pen lineWidth="0.5" lineColor="#334155"/></graphicElement>
\t\t\t\t</line>\n'''
    # Col 3: Status / Keterangan
    r += f'''\t\t\t\t<textField isStretchWithOverflow="true">
\t\t\t\t\t<reportElement positionType="Float" stretchType="RelativeToTallestObject" mode="Transparent" x="431" y="0" width="138" height="18" forecolor="#047857"/>
\t\t\t\t\t<box topPadding="4" leftPadding="4" bottomPadding="4" rightPadding="4"/>
\t\t\t\t\t<textElement textAlignment="Center" verticalAlignment="Middle">
\t\t\t\t\t\t<font fontName="Tahoma" size="7" isBold="true"/>
\t\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t\t</textElement>
\t\t\t\t\t<textFieldExpression><![CDATA[{expr_status}]]></textFieldExpression>
\t\t\t\t</textField>\n'''
    r += '\t\t\t</frame>\n'
    return r

def gen_band_penunjang():
    b = '\t\t<band height="88" splitType="Prevent">\n'
    b += section_bar(2, "PEMERIKSAAN PENUNJANG", subtitle="DIAGNOSTIK LABORATORIUM, RADIOLOGI & EKG")
    
    # Table Header Bar (y=18, h=14)
    b += rect(0, 18, 569, 14, pen="0.75", backcolor="#EBF3FA", linecolor="#334155")
    b += text(0, 18, 115, 14, "Jenis Penunjang", size=7, bold=True, align="Center", color="#0F172A")
    b += line(115, 18, 1, 14, pen="0.75", linecolor="#334155")
    b += text(121, 18, 304, 14, "Pemeriksaan / Tindakan", size=7, bold=True, color="#0F172A")
    b += line(430, 18, 1, 14, pen="0.75", linecolor="#334155")
    b += text(430, 18, 139, 14, "Status / Keterangan", size=7, bold=True, align="Center", color="#0F172A")
    
    # Row 1: Laboratorium (y=32)
    b += _row_penunjang(32, "Laboratorium", 
                        'rekammedis.RMPenilaianAwalMedisIGD.getNamaPemeriksaanLab($F{lab})',
                        'rekammedis.RMPenilaianAwalMedisIGD.getStatusPenunjangLab($F{lab})',
                        is_last=False)
    
    # Row 2: Radiologi (y=50)
    b += _row_penunjang(50, "Radiologi", 
                        'rekammedis.RMPenilaianAwalMedisIGD.getPemeriksaanRad($F{rad})',
                        'rekammedis.RMPenilaianAwalMedisIGD.getStatusRad($F{rad})',
                        is_last=False)
    
    # Row 3: EKG (y=68)
    b += _row_penunjang(68, "EKG", 
                        'rekammedis.RMPenilaianAwalMedisIGD.getPemeriksaanEKG($F{ekg})',
                        'rekammedis.RMPenilaianAwalMedisIGD.getStatusEKG($F{ekg})',
                        is_last=True)
    
    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 7: DIAGNOSIS & RENCANA / TINDAKAN / TERAPI
# ==============================================================================
def gen_band_diagnosis_tata():
    b = '\t\t<band height="162" splitType="Stretch">\n'
    b += section_bar(2, "DIAGNOSIS & RENCANA / TINDAKAN / TERAPI", subtitle="PENATALAKSANAAN MEDIS IGD")
    
    b += rect(0, 18, 569, 140, pen="0.75", linecolor="#334155", stretch_type="RelativeToTallestObject")
    b += line(254, 18, 1, 140, pen="0.75", linecolor="#334155", stretch_type="RelativeToTallestObject")
    
    # Kolom Kiri
    # Diagnosa Kerja box
    b += rect(6, 22, 242, 30, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1", radius=2)
    b += text(12, 24, 230, 11, "✓ Diagnosa Kerja:", size=7, bold=True, color="#0F172A")
    b += field(12, 35, 230, 15, '($F{diagnosa_kerja}==null||$F{diagnosa_kerja}.isEmpty()?"-":$F{diagnosa_kerja})', size=7.5, bold=True, vAlign="Top", color="#0F172A", stretch=True)
    
    # Diagnosa Banding box
    b += rect(6, 55, 242, 30, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1", radius=2)
    b += text(12, 57, 230, 11, "✓ Diagnosa Banding:", size=7, bold=True, color="#0F172A")
    b += field(12, 68, 230, 15, '($F{diagnosa_banding}==null||$F{diagnosa_banding}.isEmpty()?"-":$F{diagnosa_banding})', size=7.5, bold=True, vAlign="Top", color="#0F172A", stretch=True)
    
    # Ekspresi Edukasi
    exp_edukasi = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?\\\\b4\\\\.[ \\\\t]*edukasi[^\\\\r\\\\n]*.*")&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*edukasi[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty()&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*edukasi[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().equals(\'-\') ? '
        '$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*edukasi[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim() : '
        '("Ya".equals($F{edukasi_diberikan}) ? "Diberikan kepada Pasien / Keluarga" : ("Ya".equals($F{edukasi_tidak_bisa}) ? "Tidak dapat diberikan (" + ($F{edukasi_tidak_bisa_ket}!=null&&!$F{edukasi_tidak_bisa_ket}.isEmpty()?$F{edukasi_tidak_bisa_ket}:"-") + ")" : "-")))'
    )
    b += field(6, 88, 242, 14, f'"4. Edukasi Pasien / Keluarga: " + {exp_edukasi}', size=6.5, bold=True, color="#1E293B", stretch=True)
    
    # 5. Rencana Tindak Lanjut
    b += text(6, 104, 242, 11, "5. Rencana Tindak Lanjut:", size=7, bold=True, color="#1E293B")
    
    # Ekspresi Tindak Lanjut
    exp_tindak_lanjut = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?\\\\b5\\\\.[ \\\\t]*rencana[^\\\\r\\\\n]*.*")&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b5\\\\.[ \\\\t]*rencana[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*6\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty()&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b5\\\\.[ \\\\t]*rencana[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*6\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().equals(\'-\') ? '
        '$F{tata}.replaceAll(\'(?is)^.*?\\\\b5\\\\.[ \\\\t]*rencana[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*6\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim() : '
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?\\\\b4\\\\.[ \\\\t]*rencana[ \\\\t]+lanjutan[^\\\\r\\\\n]*.*")&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*rencana[ \\\\t]+lanjutan[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty()&&'
        '!$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*rencana[ \\\\t]+lanjutan[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().equals(\'-\') ? '
        '$F{tata}.replaceAll(\'(?is)^.*?\\\\b4\\\\.[ \\\\t]*rencana[ \\\\t]+lanjutan[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*5\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim() : '
        '($F{konsul_nm_dokter}!=null&&!$F{konsul_nm_dokter}.isEmpty() ? "Konsul DPJP: " + $F{konsul_nm_dokter} + " (" + $F{konsul_nm_poli} + ")" : "-")))'
    )
    b += field(12, 116, 236, 12, f'"• Konsul / Rencana Lanjutan: " + {exp_tindak_lanjut}', size=6.5, color="#334155", stretch=True)
    b += field(12, 129, 236, 11, '"• Pemeriksaan Lainnya: " + ($F{pemeriksaan_lainnya}==null||$F{pemeriksaan_lainnya}.isEmpty()?"-":$F{pemeriksaan_lainnya})', size=6.5, color="#334155")
    b += field(6, 141, 242, 13, '"Konsul Dokter Spesialis: " + ($F{konsul_nm_dokter}!=null&&!$F{konsul_nm_dokter}.isEmpty() ? $F{konsul_nm_dokter} + " (" + $F{konsul_nm_poli} + ")" : "-")', size=6.5, bold=True, color="#0F172A")

    # Kolom Kanan
    is_struct = '($F{tata}==null||$F{tata}.matches("(?is).*?\\\\b1\\\\.[ \\\\t]*monitoring.*")||$F{tata}.matches("(?is).*?cairan[ \\\\t]+infus.*")||$F{tata}.matches("(?is).*?\\\\b3\\\\.[ \\\\t]*(terapi|farmakologi|medikamentosa).*"))'
    not_struct = f'(!{is_struct})'

    # 1. Monitoring & Observasi
    exp_m1 = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?\\\\b1\\\\.[ \\\\t]*monitoring[^\\\\r\\\\n]*.*") ? '
        '($F{tata}.replaceAll(\'(?is)^.*?\\\\b1\\\\.[ \\\\t]*monitoring[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*2\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty() ? "-" : '
        '$F{tata}.replaceAll(\'(?is)^.*?\\\\b1\\\\.[ \\\\t]*monitoring[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*2\\\\..*|$).*\', \'$1\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim()) : "-")'
    )
    b += field(260, 22, 303, 16, f'"1. Monitoring & Observasi: " + {exp_m1}', size=6.5, bold=True, color="#1E293B", stretch=True, p_when=is_struct)

    # 2. Tindakan Medis & Keperawatan
    exp_m2 = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?\\\\b2\\\\.[ \\\\t]*(tindakan|non-farmakologi)[^\\\\r\\\\n]*.*") ? '
        '($F{tata}.replaceAll(\'(?is)^.*?\\\\b2\\\\.[ \\\\t]*(tindakan|non-farmakologi)[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*3\\\\..*|$).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty() ? "-" : '
        '$F{tata}.replaceAll(\'(?is)^.*?\\\\b2\\\\.[ \\\\t]*(tindakan|non-farmakologi)[^\\\\r\\\\n]*[\\\\r\\\\n]+(.*?)(?=[\\\\r\\\\n]+[ \\\\t]*3\\\\..*|$).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim()) : "-")'
    )
    b += field(260, 39, 303, 16, f'"2. Tindakan Medis & Keperawatan: " + {exp_m2}', size=6.5, bold=True, color="#1E293B", stretch=True, p_when=is_struct)

    # 3. Terapi Cairan & Medikamentosa
    b += text(260, 56, 303, 11, "3. Terapi Cairan & Medikamentosa:", size=7, bold=True, color="#1E293B", p_when=is_struct)

    # Table 3 rows
    b += rect(260, 69, 303, 83, pen="0.5", linecolor="#CBD5E1", p_when=is_struct)
    b += line(260, 96, 303, 1, pen="0.5", linecolor="#CBD5E1", p_when=is_struct)
    b += line(260, 124, 303, 1, pen="0.5", linecolor="#CBD5E1", p_when=is_struct)
    b += line(335, 69, 1, 83, pen="0.5", linecolor="#CBD5E1", p_when=is_struct)

    # Row 1: Cairan Infus
    exp_infus = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?(cairan[ \\\\t]+infus|infus[ \\\\t]+cairan|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*infus)[ \\\\t]*[:\\\\s][ \\\\t]*[^\\\\r\\\\n]*.*") ? '
        '($F{tata}.replaceAll(\'(?is)^.*?(cairan[ \\\\t]+infus|infus[ \\\\t]+cairan|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*infus)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty() ? "-" : '
        '$F{tata}.replaceAll(\'(?is)^.*?(cairan[ \\\\t]+infus|infus[ \\\\t]+cairan|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*infus)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim()) : "-")'
    )
    b += rect(260, 69, 75, 27, pen="0.0", backcolor="#F8FAFC", p_when=is_struct)
    b += text(264, 74, 70, 18, "Cairan Infus", size=7, bold=True, color="#1E293B", p_when=is_struct)
    b += field(339, 71, 220, 23, exp_infus, size=6.5, vAlign="Middle", color="#0F172A", stretch=True, p_when=is_struct)

    # Row 2: Injeksi
    exp_inj = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?(injeksi|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*inj\\\\.?)[ \\\\t]*[:\\\\s][ \\\\t]*[^\\\\r\\\\n]*.*") ? '
        '($F{tata}.replaceAll(\'(?is)^.*?(injeksi|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*inj\\\\.?)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty() ? "-" : '
        '$F{tata}.replaceAll(\'(?is)^.*?(injeksi|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*inj\\\\.?)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim()) : "-")'
    )
    b += rect(260, 96, 75, 28, pen="0.0", backcolor="#F8FAFC", p_when=is_struct)
    b += text(264, 101, 70, 18, "Injeksi", size=7, bold=True, color="#1E293B", p_when=is_struct)
    b += field(339, 98, 220, 24, exp_inj, size=6.5, vAlign="Middle", color="#0F172A", stretch=True, p_when=is_struct)

    # Row 3: Obat Oral
    exp_oral = (
        '($F{tata}!=null&&$F{tata}.matches("(?is).*?(obat[ \\\\t]+oral|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*oral)[ \\\\t]*[:\\\\s][ \\\\t]*[^\\\\r\\\\n]*.*") ? '
        '($F{tata}.replaceAll(\'(?is)^.*?(obat[ \\\\t]+oral|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*oral)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim().isEmpty() ? "-" : '
        '$F{tata}.replaceAll(\'(?is)^.*?(obat[ \\\\t]+oral|[ \\\\t\\\\r\\\\n]-?[ \\\\t]*oral)[ \\\\t]*[:\\\\s][ \\\\t]*([^\\\\r\\\\n]*).*\', \'$2\').replaceAll(\'^[ \\\\t]*-+[ \\\\t]*\', \'\').trim()) : "-")'
    )
    b += rect(260, 124, 75, 28, pen="0.0", backcolor="#F8FAFC", p_when=is_struct)
    b += text(264, 129, 70, 18, "Obat Oral", size=7, bold=True, color="#1E293B", p_when=is_struct)
    b += field(339, 126, 220, 24, exp_oral, size=6.5, vAlign="Middle", color="#0F172A", stretch=True, p_when=is_struct)

    # Fallback Narasi Bebas
    b += text(260, 22, 303, 12, "Instruksi Penatalaksanaan Medis / Rencana Terapi:", size=7, bold=True, color="#1E293B", p_when=not_struct)
    b += rect(260, 37, 303, 115, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1", radius=2, p_when=not_struct)
    b += field(264, 39, 295, 111, '($F{tata}==null||$F{tata}.isEmpty()?"-":$F{tata})', size=6.5, vAlign="Top", color="#0F172A", stretch=True, p_when=not_struct)

    b += '\t\t</band>\n'
    return b

# ==============================================================================
# BAND 8: EDUKASI, KONDISI SAAT PULANG, TTV TERAKHIR & TTD DOKTER
# ==============================================================================
def gen_band_edukasi_ttd():
    b = '\t\t<band height="162" splitType="Prevent">\n'
    b += section_bar(2, "EDUKASI, KONDISI SAAT INI / KELUAR IGD & TANDA VITAL TERAKHIR", subtitle="EVALUASI AKHIR & VERIFIKASI DPJP")
    
    b += rect(0, 18, 569, 140, pen="0.75", linecolor="#334155")
    b += line(375, 18, 1, 140, pen="0.75", linecolor="#334155")
    
    # Sub-kiri
    b += text(6, 22, 360, 11, "Status Edukasi Pasien / Keluarga:", size=7, bold=True, color="#1E293B")
    b += cb(6, 34, 350, 11, "Edukasi Diberikan kepada Pasien / Keluarga", "$F{edukasi_diberikan}")
    b += cb(6, 47, 180, 11, "Tidak Dapat Memberikan Edukasi Karena:", "$F{edukasi_tidak_bisa}")
    b += field(188, 47, 175, 11, '($F{edukasi_tidak_bisa_ket}==null||$F{edukasi_tidak_bisa_ket}.isEmpty()?"-":$F{edukasi_tidak_bisa_ket})', size=7)
    
    # Kondisi Saat Ini / Keluar IGD Box
    b += rect(6, 63, 358, 17, pen="0.5", backcolor="#F8FAFC", linecolor="#CBD5E1", radius=2)
    b += field(10, 64, 350, 15, '"Kondisi Saat Ini / Keluar IGD: " + ($F{kondisi_pulang}==null||$F{kondisi_pulang}.isEmpty()?"-":$F{kondisi_pulang})', size=7, bold=True, color="#0F172A")
    
    # Tanda Vital Terakhir
    b += text(6, 84, 350, 11, "Tanda Vital Terakhir:", size=7, bold=True, color="#1E293B")
    
    # 4 Cards TTV menggunakan ttv_card
    b += ttv_card(6, 97, 86, 36, "TD", '($F{td_akhir}!=null&&!$F{td_akhir}.isEmpty()?$F{td_akhir}+" mmHg":"-")')
    b += ttv_card(96, 97, 86, 36, "Nadi", '($F{nadi_akhir}!=null&&!$F{nadi_akhir}.isEmpty()?$F{nadi_akhir}+" x/m":"-")')
    b += ttv_card(186, 97, 86, 36, "RR", '($F{rr_akhir}!=null&&!$F{rr_akhir}.isEmpty()?$F{rr_akhir}+" x/m":"-")')
    b += ttv_card(276, 97, 88, 36, "Suhu", '($F{suhu_akhir}!=null&&!$F{suhu_akhir}.isEmpty()?$F{suhu_akhir}+" °C":"-")')

    # Sub-kanan: Kotak Tanda Tangan Dokter (TTE) - Format Resmi RS Pemerintah
    tgl_dok_expr = '"Tanggal: " + ($F{tanggal}==null?"-":new java.text.SimpleDateFormat("dd/MM/yyyy").format($F{tanggal})) + " • Pukul: " + ($F{tanggal}==null?"-":new java.text.SimpleDateFormat("HH:mm:ss").format($F{tanggal}))'
    b += field(378, 22, 188, 11, tgl_dok_expr, size=6.5, align="Center", color="#000000")
    b += text(378, 34, 188, 12, "Dokter Penanggung Jawab Pelayanan", size=7, bold=True, align="Center", color="#000000")
    
    # Box Dashed TTE
    b += f'''\t\t\t<rectangle radius="2">
\t\t\t\t<reportElement positionType="Float" mode="Transparent" x="388" y="50" width="168" height="26"/>
\t\t\t\t<graphicElement><pen lineWidth="0.5" lineStyle="Dashed" lineColor="#64748B"/></graphicElement>
\t\t\t</rectangle>\n'''
    b += f'''\t\t\t<staticText>
\t\t\t\t<reportElement positionType="Float" x="390" y="53" width="164" height="20" forecolor="#000000"/>
\t\t\t\t\t<textElement textAlignment="Center" verticalAlignment="Middle">
\t\t\t\t\t<font fontName="Tahoma" size="6.5" isBold="true"/>
\t\t\t\t\t<paragraph lineSpacing="Single"/>
\t\t\t\t</textElement>
\t\t\t\t<text><![CDATA[[ Tanda Tangan Elektronik /
Terverifikasi ]]]></text>
\t\t\t</staticText>\n'''
    
    # Nama Dokter dihitamkan (#000000) agar resmi
    b += field(378, 82, 188, 13, '$F{nm_dokter}', size=7.5, bold=True, align="Center", color="#000000")
    b += line(405, 95, 134, 1, pen="0.5", linecolor="#000000")
    
    # NIP & SIP: SIP berada di bawah NIP
    exp_nip_sip = (
        '($F{nip_dokter}!=null&&!$F{nip_dokter}.trim().isEmpty()&&!$F{nip_dokter}.trim().equals("-") ? '
        '($F{nip_dokter}.trim().toUpperCase().startsWith("NIP") ? $F{nip_dokter}.trim() : "NIP. " + $F{nip_dokter}.trim()) : "") + '
        '($F{no_ijn_praktek}!=null&&!$F{no_ijn_praktek}.trim().isEmpty()&&!$F{no_ijn_praktek}.trim().equals("-") ? '
        '($F{nip_dokter}!=null&&!$F{nip_dokter}.trim().isEmpty()&&!$F{nip_dokter}.trim().equals("-") ? "\\n" : "") + '
        '($F{no_ijn_praktek}.trim().toUpperCase().startsWith("SIP") ? $F{no_ijn_praktek}.trim() : "SIP. " + $F{no_ijn_praktek}.trim()) : "")'
    )
    b += field(378, 98, 188, 22, exp_nip_sip, size=6.5, align="Center", vAlign="Top", color="#000000", stretch=True)

    b += '\t\t</band>\n'
    return b

def main():
    detail_bands = (
        '\t<detail>\n' +
        gen_band_header() +
        gen_band_prehospital() +
        gen_band_survei_primer() +
        gen_band_anamnesis() +
        gen_band_bayi_pat() +
        gen_band_lokalis_fisik() +
        gen_band_penunjang() +
        gen_band_diagnosis_tata() +
        gen_band_edukasi_ttd() +
        '\t</detail>\n'
    )
    full_xml = xml_header + gen_page_header() + detail_bands + gen_page_footer() + '</jasperReport>'
    out_file = "report/rptCetakPenilaianAwalMedisIGD.jrxml"
    with open(out_file, "w", encoding="utf-8") as f:
        f.write(full_xml)
    print(f"File {out_file} berhasil dibuat dengan sukses.")

if __name__ == "__main__":
    main()
