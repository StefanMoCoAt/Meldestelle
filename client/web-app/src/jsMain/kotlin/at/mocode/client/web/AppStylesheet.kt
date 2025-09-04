@file:OptIn(org.jetbrains.compose.web.ExperimentalComposeWebApi::class)

package at.mocode.client.web

import org.jetbrains.compose.web.css.*

object AppStylesheet : StyleSheet() {
    val container by style {
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Column)
        minHeight(100.vh)
        fontFamily("'Segoe UI', system-ui, sans-serif")
        margin(0.px)
        padding(0.px)
        backgroundColor(Color("#f5f5f5"))
    }

    val header by style {
        backgroundColor(Color("#1976d2"))
        color(Color.white)
        padding(20.px)
        textAlign("center")
        property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
    }

    val main by style {
        flex(1)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.Center)
        alignItems(AlignItems.Center)
        padding(40.px, 20.px)
    }

    val footer by style {
        backgroundColor(Color("#333"))
        color(Color.white)
        textAlign("center")
        padding(20.px)
        fontSize(14.px)
    }

    val card by style {
        backgroundColor(Color.white)
        borderRadius(12.px)
        property("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
        padding(32.px)
        maxWidth(500.px)
        width(100.percent)
        textAlign("center")
    }

    val button by style {
        border(0.px)
        borderRadius(8.px)
        padding(12.px, 24.px)
        fontSize(16.px)
        fontWeight("bold")
        cursor("pointer")
        property("transition", "all 0.2s ease")
        width(100.percent)
        marginBottom(20.px)
    }

    val buttonHover by style {
        transform { scale(1.02) }
    }

    val buttonDisabled by style {
        opacity(0.6)
        cursor("not-allowed")
    }

    val primaryButton by style {
        backgroundColor(Color("#1976d2"))
        color(Color.white)

        hover(self) style {
            backgroundColor(Color("#1565c0"))
        }
    }

    val successMessage by style {
        backgroundColor(Color("#e8f5e8"))
        color(Color("#2e7d32"))
        padding(16.px)
        borderRadius(8.px)
        marginTop(16.px)
        border(1.px, LineStyle.Solid, Color("#c8e6c9"))
    }

    val errorMessage by style {
        backgroundColor(Color("#ffebee"))
        color(Color("#c62828"))
        padding(16.px)
        borderRadius(8.px)
        marginTop(16.px)
        border(1.px, LineStyle.Solid, Color("#ffcdd2"))
    }

    val spinner by style {
        display(DisplayStyle.InlineBlock)
        width(16.px)
        height(16.px)
        border(2.px, LineStyle.Solid, Color("#f3f3f3"))
        property("border-top", "2px solid #1976d2")
        borderRadius(50.percent)
        property("animation", "spin 1s linear infinite")
        marginRight(8.px)
        property("vertical-align", "middle")
    }

}
