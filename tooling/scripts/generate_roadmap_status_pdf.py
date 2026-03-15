from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import mm
from reportlab.platypus import Paragraph, SimpleDocTemplate, Spacer, Table, TableStyle


ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "output" / "pdf" / "cassy_roadmap_status_sync_2026_03_16.pdf"


def bullet(text: str) -> Paragraph:
    return Paragraph(f"- {text}", styles["BodyText"])


styles = getSampleStyleSheet()
styles.add(
    ParagraphStyle(
        name="CassyTitle",
        parent=styles["Title"],
        fontName="Helvetica-Bold",
        fontSize=20,
        leading=24,
        textColor=colors.HexColor("#143642"),
        spaceAfter=8,
    )
)
styles.add(
    ParagraphStyle(
        name="CassyHeading",
        parent=styles["Heading2"],
        fontName="Helvetica-Bold",
        fontSize=12,
        leading=15,
        textColor=colors.HexColor("#143642"),
        spaceBefore=8,
        spaceAfter=6,
    )
)
styles["BodyText"].fontName = "Helvetica"
styles["BodyText"].fontSize = 9
styles["BodyText"].leading = 13
styles["BodyText"].spaceAfter = 4


def build_pdf() -> None:
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    doc = SimpleDocTemplate(
        str(OUTPUT),
        pagesize=A4,
        leftMargin=18 * mm,
        rightMargin=18 * mm,
        topMargin=16 * mm,
        bottomMargin=16 * mm,
        title="Cassy Roadmap Status Sync",
        author="Codex",
    )

    story = [
        Paragraph("Cassy Roadmap Status Sync", styles["CassyTitle"]),
        Paragraph(
            "Repo-local PDF ini adalah sinkronisasi status roadmap terhadap evidence repo per 2026-03-16. "
            "Dokumen ini tidak mengubah PDF eksternal lama; dokumen ini menjadi artefak jujur yang dapat diregenerasi dari repo.",
            styles["BodyText"],
        ),
        Spacer(1, 4),
        Table(
            [
                ["Tanggal", "2026-03-16"],
                ["Posture", "Desktop-first retail operating core"],
                ["Primary pilot OS", "Windows"],
                ["Highest fully stable milestone", "M1"],
                ["Highest operationally advanced milestone", "thin M5"],
            ],
            colWidths=[58 * mm, 104 * mm],
        ),
    ]

    story[-1].setStyle(
        TableStyle(
            [
                ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#E8F1F2")),
                ("BACKGROUND", (0, 0), (0, -1), colors.HexColor("#F4F7F7")),
                ("BOX", (0, 0), (-1, -1), 0.5, colors.HexColor("#B9C7CC")),
                ("INNERGRID", (0, 0), (-1, -1), 0.25, colors.HexColor("#D5DEE0")),
                ("FONTNAME", (0, 0), (-1, -1), "Helvetica"),
                ("FONTSIZE", (0, 0), (-1, -1), 9),
                ("VALIGN", (0, 0), (-1, -1), "TOP"),
                ("TOPPADDING", (0, 0), (-1, -1), 6),
                ("BOTTOMPADDING", (0, 0), (-1, -1), 6),
            ]
        )
    )

    story.extend(
        [
            Spacer(1, 10),
            Paragraph("Milestone reality", styles["CassyHeading"]),
            bullet("M0 DONE"),
            bullet("M1 DONE"),
            bullet("M2 PARTIAL / FOUNDATION"),
            bullet("M3 PARTIAL / FOUNDATION"),
            bullet("M4 PARTIAL / FOUNDATION"),
            bullet("thin M5 PARTIAL / FOUNDATION"),
            Paragraph("False readiness removed", styles["CassyHeading"]),
            bullet("Desktop source set false-ready ditutup; lane desktop sekarang benar-benar membuild src/jvmMain."),
            bullet("M3 dan M4 tidak lagi dianggap selesai hanya karena UI tampil."),
            bullet("Debian package tidak lagi diperlakukan sebagai bukti readiness Windows."),
            bullet("Desktop run Java 21 drift dan Skiko crash ditutup; policy JDK 17 kembali enforced."),
            Paragraph("Verification evidence", styles["CassyHeading"]),
            bullet(r".\gradlew --version -> Java 17 launcher + Java 17 daemon criteria"),
            bullet(r".\gradlew clean -> success"),
            bullet(r".\gradlew build -> success"),
            bullet(r".\gradlew test -> success"),
            bullet(r".\gradlew detekt -> success"),
            bullet(r".\gradlew :apps:android-pos:lintDebug -> success"),
            bullet(r".\gradlew :apps:desktop-pos:smokeRun -> CASSY_SMOKE_OK stage=Bootstrap"),
            bullet(r".\gradlew :apps:desktop-pos:createDistributable -> success"),
            bullet(r".\gradlew :apps:desktop-pos:packageDistributionForCurrentOS -> EXE generated"),
            bullet("SQLDelight migration verification Windows -> success with sqlite worker initializer"),
            Paragraph("Remaining gaps", styles["CassyHeading"]),
            bullet("Hosted Windows CI execution evidence belum terlihat dari environment lokal ini."),
            bullet("Smoke install/uninstall installer Windows masih manual dan belum tervalidasi end-to-end."),
            bullet(":shared masih legacy bridge dan belum selesai disusutkan."),
            bullet("Checkout final, payment final, receipt final, reporting dasar, sync visibility, dan migration replay belum done."),
            Paragraph("Regeneration path", styles["CassyHeading"]),
            bullet("Source markdown: docs/execution/roadmap_pdf_source_2026_03_16.md"),
            bullet("Generator: tooling/scripts/generate_roadmap_status_pdf.py"),
            bullet("Output: output/pdf/cassy_roadmap_status_sync_2026_03_16.pdf"),
        ]
    )

    doc.build(story)


if __name__ == "__main__":
    build_pdf()
