package ru.dejaview.search.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.dejaview.contracts.session.SessionState
import ru.dejaview.model.SearchMode
import ru.dejaview.navigation.SearchRoute

class SearchAccessTest {
    private val session = TestSession()
    private val history = TestHistory()
    private val vm = SearchViewModel(session, history)

    @Test
    fun guestOpensTextWhileAllMediaModesRequireLogin() {
        assertTrue(vm.canOpen(SearchMode.TEXT))
        assertNull(vm.requestLogin(SearchRoute(SearchMode.TEXT)))
        SearchMode.entries.filter { it != SearchMode.TEXT }.forEach { mode ->
            assertFalse(vm.canOpen(mode))
            assertNotNull(vm.requestLogin(SearchRoute(mode)))
        }
        assertTrue(history.writes.isEmpty())
    }

    @Test
    fun loginReturnsToTheSelectedModeAndDraftOnce() {
        val route = SearchRoute(SearchMode.IMAGE, "draft-1")
        val id = vm.requestLogin(route)
        session.state.value = signedIn()

        assertEquals(route, vm.finishLogin(id))
        assertNull(vm.finishLogin(id))
        assertNull(vm.requestLogin(route))
    }

    @Test
    fun cancellingOrFailingLoginDoesNotOpenThePendingMediaMode() {
        val route = SearchRoute(SearchMode.AUDIO, "draft-2")
        val cancelled = vm.requestLogin(route)
        vm.cancelLogin(cancelled)
        val expired = vm.requestLogin(route)
        session.state.value = SessionState.Expired

        assertFalse(vm.canOpen(route.mode))
        assertNull(vm.finishLogin(expired))
        session.state.value = signedIn()
        assertNull(vm.finishLogin(cancelled))
        assertNull(vm.finishLogin(expired))
    }
}
