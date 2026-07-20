package com.puravida.modules.reports.infrastructure.pdf;

import com.puravida.modules.reports.application.dto.WeeklyReportSummary;
import com.puravida.modules.reports.application.dto.WeeklyReportTopDish;
import com.puravida.modules.reports.application.port.out.WeeklyReportPdfGeneratorPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportPdfGenerator implements WeeklyReportPdfGeneratorPort {

    private static final float LEFT_MARGIN = 50F;
    private static final float FIRST_LINE_Y = 740F;
    private static final float LINE_HEIGHT = 15F;
    private static final Locale MEXICO = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PDFont normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public byte[] generate(WeeklyReportSummary report) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage(PDRectangle.LETTER));
            try (PDPageContentStream content = new PDPageContentStream(document, document.getPage(0))) {
                Cursor cursor = new Cursor(FIRST_LINE_Y);
                write(content, cursor, "PuraVida", boldFont, 18F);
                write(content, cursor, "Reporte semanal de ventas", boldFont, 14F);
                write(content, cursor, "Rango: " + report.semanaInicio() + " a " + report.semanaFin(), normalFont, 10F);
                write(content, cursor, "Generado: " + DATE_TIME_FORMAT.format(report.generadoEn()), normalFont, 10F);
                gap(cursor);

                section(content, cursor, "Resumen de ventas");
                write(content, cursor, "Total vendido activo: " + money(report.ventas().totalActivo()), normalFont, 10F);
                write(content, cursor, "Cantidad de ventas activas: " + report.ventas().cantidadActivas(), normalFont, 10F);
                write(content, cursor, "Total anulado: " + money(report.ventas().totalAnulado()), normalFont, 10F);
                write(content, cursor, "Cantidad de ventas anuladas: " + report.ventas().cantidadAnuladas(), normalFont, 10F);
                gap(cursor);

                section(content, cursor, "Ventas por fuente");
                write(content, cursor, "Manual fonda: " + report.ventas().manuales().cantidad()
                        + " ventas, " + money(report.ventas().manuales().total()), normalFont, 10F);
                write(content, cursor, "Remota: " + report.ventas().remotas().cantidad()
                        + " ventas, " + money(report.ventas().remotas().total()), normalFont, 10F);
                gap(cursor);

                section(content, cursor, "Pedidos operativos");
                write(content, cursor, "Pendientes: " + report.pedidos().pendientes(), normalFont, 10F);
                write(content, cursor, "Aceptados: " + report.pedidos().aceptados(), normalFont, 10F);
                write(content, cursor, "Rechazados: " + report.pedidos().rechazados(), normalFont, 10F);
                write(content, cursor, "Cancelados: " + report.pedidos().cancelados(), normalFont, 10F);
                gap(cursor);

                section(content, cursor, "Top de platillos vendidos");
                if (report.topPlatillos().isEmpty()) {
                    write(content, cursor, "Sin ventas válidas con detalle de platillos en este periodo.", normalFont, 10F);
                } else {
                    int position = 1;
                    for (WeeklyReportTopDish dish : report.topPlatillos()) {
                        write(content, cursor, position + ". " + truncate(dish.nombre(), 48)
                                + " - " + dish.cantidadVendida() + " unidades - "
                                + money(dish.totalGenerado()), normalFont, 10F);
                        position++;
                    }
                }
                gap(cursor);

                section(content, cursor, "Notas");
                write(content, cursor, "Las ventas anuladas no se suman al total activo.", normalFont, 9F);
                write(content, cursor, "El top integra el detalle histórico de ventas manuales y remotas válidas.", normalFont, 9F);
                write(content, cursor, "VENTAS es la fuente oficial para los importes económicos.", normalFont, 9F);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el reporte semanal PDF.", exception);
        }
    }

    private void section(PDPageContentStream content, Cursor cursor, String title) throws IOException {
        write(content, cursor, title, boldFont, 11F);
    }

    private void write(PDPageContentStream content, Cursor cursor, String value, PDFont font, float size) throws IOException {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(LEFT_MARGIN, cursor.y);
        content.showText(value);
        content.endText();
        cursor.y -= LINE_HEIGHT;
    }

    private void gap(Cursor cursor) {
        cursor.y -= 4F;
    }

    private String money(BigDecimal amount) {
        return java.text.NumberFormat.getCurrencyInstance(MEXICO).format(amount);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "Sin nombre" : value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private static class Cursor {

        private float y;

        private Cursor(float y) {
            this.y = y;
        }
    }
}
