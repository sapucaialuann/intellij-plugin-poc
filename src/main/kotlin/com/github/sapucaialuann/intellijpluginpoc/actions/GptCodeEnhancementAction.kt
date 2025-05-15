package com.github.sapucaialuann.intellijpluginpoc.actions

import com.github.sapucaialuann.intellijpluginpoc.services.GPTService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction

class GptCodeEnhancementAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val document = editor.document
        val selectionModel = editor.selectionModel

        val selectedText = selectionModel.selectedText ?: return

        val prompt = buildPrompt(selectedText)

        ApplicationManager.getApplication().executeOnPooledThread {
            val response = GPTService.callGpt(prompt)
            val cleanResponse = sanitizeResponse(response)
            ApplicationManager.getApplication().invokeLater {
                WriteCommandAction.runWriteCommandAction(project) {
                    val start = selectionModel.selectionStart
                    val end = selectionModel.selectionEnd
                    document.replaceString(start, end, cleanResponse)
                }
            }
        }
    }

    private fun sanitizeResponse(response: String): String {
        return response
            .replace(Regex("(?s)```.*?\\n(.*?)```"), "$1") // removes ```language\n...\n```
            .trim()
    }

    private fun buildPrompt(code: String): String {
        return """
            You are an expert developer. Please analyse the following code:

            $code

            Refactor it using the best practices. Nothing but code and comments are allowed.
        """.trimIndent()
    }
}