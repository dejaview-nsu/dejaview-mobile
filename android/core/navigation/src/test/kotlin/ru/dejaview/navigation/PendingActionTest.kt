package ru.dejaview.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.dejaview.model.MovieList
import ru.dejaview.model.SearchMode

class PendingActionTest {
    @Test fun mismatchDuplicateCancelAndRestartNeverReplayAnAction() {
        val store = PendingActionStore()
        val media = PendingAction.OpenMode(SearchMode.AUDIO, "draft")
        val membership = PendingAction.SetMembership(4, MovieList.WATCHED, true)
        val rating = PendingAction.SetRating(4, null)
        val stale = store.expect(media)
        val current = store.expect(membership)
        assertNull(store.consume(stale))
        assertEquals(membership, store.consume(current))
        assertNull(store.consume(current))
        val cancelled = store.expect(rating)
        store.clear()
        assertNull(store.consume(cancelled))
        val fresh = store.expect(media)
        assertNull(PendingActionStore().consume(fresh))
        assertEquals(media, store.consume(fresh))
    }
}
