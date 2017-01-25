package co.zsmb.cleannotes.domain

import io.reactivex.Single

interface NotesRepository {

    fun get(noteId: Int): Single<DomainNote>

    fun getAll(): Single<List<DomainNote>>

    fun add(note: DomainNote): Single<Int>

    fun addAll(notes: List<DomainNote>): Single<List<Int>>

    fun update(note: DomainNote): Single<Boolean>

    fun updateAll(notes: List<DomainNote>): Single<Int>

    fun delete(noteId: Int): Single<Boolean>

    fun deleteAll(noteIds: List<Int>): Single<Int>

}
