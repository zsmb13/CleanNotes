package co.zsmb.cleannotes.data

import io.reactivex.Single
import io.realm.Realm

class NoteDataSourceDisk : NoteDataSource {

    override fun add(note: RealmNote): Single<Int> {
        withRealmTransaction {
            note.giveId(this)
            insert(note)
        }
        return Single.just(note.id)
    }

    override fun addAll(notes: List<RealmNote>): Single<List<Int>> {
        // TODO check for failure somehow
        withRealmTransaction {
            notes.giveIds(this)
            insert(notes)
        }
        val ids = notes.map { it.id }
        return Single.just(ids)
    }

    override fun delete(noteId: Int): Single<Boolean> {
        // TODO check for failure somehow
        withRealmTransaction {
            where(RealmNote::class.java).equalTo("id", noteId).findFirst().deleteFromRealm()
        }
        return Single.just(true)
    }

    override fun deleteAll(noteIds: List<Int>): Single<Int> {
        if (noteIds.isEmpty()) {
            return Single.just(0)
        }

        withRealmTransaction {
            where(RealmNote::class.java).`in`("id", noteIds.toTypedArray()).findAll().deleteAllFromRealm()
        }
        return Single.just(noteIds.size)
    }

    override fun getAll(): Single<List<RealmNote>> {
        val notes = withRealm {
            where(RealmNote::class.java).findAll().map { copyFromRealm(it) }
        }
        return Single.just(notes)
    }

    override fun get(noteId: Int): Single<RealmNote> {
        val note = withRealm {
            val managedNote = where(RealmNote::class.java).equalTo("id", noteId).findFirst()

            if (managedNote == null) null else copyFromRealm(managedNote)
        }

        if (note == null) {
            return Single.error(RuntimeException("No note exists with id $noteId"))
        }
        else {
            return Single.just(note)
        }
    }

    override fun update(note: RealmNote): Single<Boolean> {
        // TODO check for failure somehow
        withRealmTransaction {
            insertOrUpdate(note)
        }
        return Single.just(true)
    }

    override fun updateAll(notes: List<RealmNote>): Single<Int> {
        // TODO check for failure somehow
        withRealmTransaction {
            insertOrUpdate(notes)
        }
        return Single.just(notes.size)
    }

    private fun createNewId(realm: Realm): Int {
        val maxId = realm.where(RealmNote::class.java).max("id") ?: return 0
        return maxId.toInt() + 1
    }

    private fun RealmNote.giveId(realm: Realm) {
        this.id = createNewId(realm)
    }

    private fun Iterable<RealmNote>.giveIds(realm: Realm) {
        var generatedId = createNewId(realm)
        forEach {
            it.id = generatedId++
        }
    }

    private inline fun <T> withRealm(operation: Realm.() -> T): T {
        val realm = Realm.getDefaultInstance()
        realm.use { return it.operation() }
    }

    private inline fun <T> withRealmTransaction(operation: Realm.() -> T): T {
        var result: T? = null
        var exception: Exception? = null

        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        try {
            result = realm.operation()
            realm.commitTransaction()
        } catch(e: Exception) {
            exception = e
            realm.cancelTransaction()
        } finally {
            realm.close()
            exception?.let { throw it }
        }

        return result as T
    }

}

