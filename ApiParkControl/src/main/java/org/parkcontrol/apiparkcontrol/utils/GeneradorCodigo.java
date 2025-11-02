package org.parkcontrol.apiparkcontrol.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public class GeneradorCodigo {

    private QRCodeWriter qrCodeWriter;
    public GeneradorCodigo(){
        this.qrCodeWriter = new QRCodeWriter();
    }

    public String getCodigo(String prefijo, LocalDate fecha, long contador){
        return prefijo + "-" + fecha + "-" + contador;
    }

    public String getCode(){
        return UUID.randomUUID().toString();
    }

    public String getFolio(Long idSucursal){
        String prefijo = idSucursal.toString();
        String year = String.valueOf(LocalDate.now().getYear()).substring(2);
        int diaJuliano = LocalDate.now().getDayOfYear();
        int random = (int) (Math.random() * 9000) + 1000;
        return String.format("%s-%s%03d-%d", prefijo, year, diaJuliano, random);
    }

    public String generateQR(String folio) throws IOException, WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(folio, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        String base64Image = Base64.getEncoder().encodeToString(pngData);
        return "{\"qr\":\"data:image/png;base64," + base64Image + "\"}";
    }
}
