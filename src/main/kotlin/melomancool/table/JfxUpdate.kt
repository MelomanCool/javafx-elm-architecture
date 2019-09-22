package melomancool.table.jfx

import java.util.concurrent.BlockingQueue

import melomancool.table.BoxView
import melomancool.table.ButtonView
import melomancool.table.TextFieldView
import melomancool.table.View

fun <Mes> makeNode(view: View<Mes>, mq: BlockingQueue<Mes>): Node {
    return when(view) {
        is TextFieldView ->
            TextField.new(view, mq)
        is ButtonView ->
            Button.new(view, mq)
        is BoxView -> {
            val vbox = javafx.scene.layout.VBox()
            val nodes = view.children.map{ makeNode(it, mq) }
            vbox.getChildren().addAll(nodes.map{ it.jfx })
            Box(vbox, nodes)
        }
    }
}

fun <Mes> replaceChild(
    view: View<Mes>,
    mq: BlockingQueue<Mes>,
    jfxParent: javafx.scene.layout.Pane
): Node {
    val child = makeNode(view, mq)
    jfxParent.getChildren().set(0, child.jfx)
    // jfxParent.getChildren().addAll(child.jfx)
    return child
}

fun <Mes> updateNode(
    view: View<Mes>,
    node: Node,
    mq: BlockingQueue<Mes>,
    jfxParent: javafx.scene.layout.Pane
): Node {
    return when (node) {
        is TextField ->
            when (view) {
                is TextFieldView -> {
                    javafx.application.Platform.runLater {
                        if (node.jfx.getText() != view.text) {
                            node.jfx.setText(view.text)
                        }
                    }
                    node.copy(text = view.text)
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        is Button ->
            when (view) {
                is ButtonView -> {
                    javafx.application.Platform.runLater {
                        if (node.jfx.getText() != view.text) {
                            node.jfx.setText(view.text)
                        }
                    }
                    node.copy(text = view.text)
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
        is Box ->
            when (view) {
                is BoxView -> {
                    val results = view.children.mapIndexed{ i, x ->
                        updateNode(x, node.children[i], mq, node.jfx)
                    }
                    javafx.application.Platform.runLater {
                        node.jfx.getChildren().setAll(results.map{ it.jfx })
                    }
                    node.copy(children = results)
                }
                else ->
                    replaceChild(view, mq, jfxParent)
            }
    }
}
