package ru.dejaview.auth.presentation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.dejaview.auth.domain.LoginResult
import ru.dejaview.auth.domain.LoginService
import ru.dejaview.auth.domain.validLogin
import ru.dejaview.contracts.AppError

@OptIn(ExperimentalCoroutinesApi::class)
class LoginFormTest {
    @Test fun usesLoginRulesAndAcceptsExistingShortPassword() =
        runTest {
            assertFalse(validLogin("ab"))
            assertFalse(validLogin("a..b@example.test"))
            assertFalse(validLogin("a@b..test"))
            assertFalse(validLogin("é@example.test"))
            assertTrue(validLogin("a+b@example.test"))
            assertTrue(validLogin("user_123"))
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                var calls = 0
                val result = CompletableDeferred<LoginResult>()
                val service =
                    object : LoginService {
                        override suspend fun login(
                            login: String,
                            password: String,
                            captchaToken: String?,
                        ): LoginResult {
                            calls++
                            return result.await()
                        }
                    }
                val form = LoginViewModel(service)
                form.submit("ab", "x", null)
                assertEquals("login", (form.state.value.error as AppError.Validation).field)
                form.submit("user", "", null)
                assertEquals("password", (form.state.value.error as AppError.Validation).field)
                form.submit("user", "x", null)
                form.submit("user", "x", null)
                runCurrent()
                assertTrue(form.state.value.busy)
                assertEquals(1, calls)
                result.complete(LoginResult.Success)
                runCurrent()
                assertTrue(form.state.value.complete)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test fun requiresFreshChallengeAndRespectsServerRetryDeadline() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val tokens = mutableListOf<String?>()
                val service =
                    object : LoginService {
                        override suspend fun login(
                            login: String,
                            password: String,
                            captchaToken: String?,
                        ): LoginResult {
                            tokens.add(captchaToken)
                            return when (tokens.size) {
                                1 -> LoginResult.Rejected(AppError.Forbidden("AUTH_CAPTCHA_REQUIRED"), true)
                                2 -> LoginResult.Rejected(AppError.Forbidden("AUTH_CAPTCHA_INVALID"), true)
                                3 -> LoginResult.Rejected(AppError.RateLimit(3))
                                else -> LoginResult.Success
                            }
                        }
                    }
                val form = LoginViewModel(service) { testScheduler.currentTime }

                fun attempt(token: String? = null) {
                    form.submit("viewer", "x", token)
                    runCurrent()
                }
                attempt()
                attempt()
                attempt("first")
                attempt("second")
                attempt()
                assertEquals(3L, form.state.value.waitSeconds)
                advanceTimeBy(2999)
                attempt()
                assertEquals(3, tokens.size)
                advanceTimeBy(1)
                runCurrent()
                attempt()
                assertEquals(listOf(null, "first", "second", null), tokens)
                assertTrue(form.state.value.complete)
            } finally {
                Dispatchers.resetMain()
            }
        }
}
