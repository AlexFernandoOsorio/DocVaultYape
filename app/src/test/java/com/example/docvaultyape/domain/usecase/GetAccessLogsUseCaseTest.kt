package com.example.docvaultyape.domain.usecase

import app.cash.turbine.test
import com.example.docvaultyape.domain.model.AccessAction
import com.example.docvaultyape.domain.model.AccessLog
import com.example.docvaultyape.domain.repository.DocumentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetAccessLogsUseCaseTest {

    private val repository: DocumentRepository = mockk()
    private lateinit var useCase: GetAccessLogsUseCase

    private val fakeLogs = listOf(
        AccessLog("log-1", "doc-1", Date(), AccessAction.VIEW),
        AccessLog("log-2", "doc-1", Date(), AccessAction.VIEW),
        AccessLog("log-3", "doc-1", Date(), AccessAction.DELETE_ATTEMPT)
    )

    @Before
    fun setUp() {
        useCase = GetAccessLogsUseCase(repository)
    }

    @Test
    fun `invoke retorna logs del documento`() = runTest {
        every { repository.getAccessLogs("doc-1") } returns flowOf(fakeLogs)

        useCase("doc-1").test {
            val result = awaitItem()
            assertEquals(3, result.size)
            awaitComplete()
        }

        verify(exactly = 1) { repository.getAccessLogs("doc-1") }
    }

    @Test
    fun `invoke retorna lista vacia cuando no hay logs`() = runTest {
        every { repository.getAccessLogs("doc-sin-logs") } returns flowOf(emptyList())

        useCase("doc-sin-logs").test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna logs ordenados correctamente`() = runTest {
        every { repository.getAccessLogs("doc-1") } returns flowOf(fakeLogs)

        useCase("doc-1").test {
            val result = awaitItem()
            assertEquals("log-1", result.first().id)
            assertEquals("log-3", result.last().id)
            awaitComplete()
        }
    }

    @Test
    fun `invoke filtra logs por documentId correcto`() = runTest {
        every { repository.getAccessLogs("doc-1") } returns flowOf(fakeLogs)
        every { repository.getAccessLogs("doc-2") } returns flowOf(emptyList())

        useCase("doc-1").test {
            val result = awaitItem()
            assertTrue(result.all { it.documentId == "doc-1" })
            awaitComplete()
        }

        useCase("doc-2").test {
            assertEquals(0, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna logs con accion VIEW`() = runTest {
        val soloViews = fakeLogs.filter { it.action == AccessAction.VIEW }
        every { repository.getAccessLogs("doc-1") } returns flowOf(soloViews)

        useCase("doc-1").test {
            val result = awaitItem()
            assertTrue(result.all { it.action == AccessAction.VIEW })
            awaitComplete()
        }
    }

    @Test
    fun `invoke retorna logs con diferentes acciones`() = runTest {
        every { repository.getAccessLogs("doc-1") } returns flowOf(fakeLogs)

        useCase("doc-1").test {
            val result = awaitItem()
            val actions = result.map { it.action }
            assertTrue(actions.contains(AccessAction.VIEW))
            assertTrue(actions.contains(AccessAction.DELETE_ATTEMPT))
            awaitComplete()
        }
    }
}
