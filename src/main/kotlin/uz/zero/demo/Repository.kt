package uz.zero.demo

import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}


class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}



@Repository
interface CategoryRepository : BaseRepository<Category> {
    @Query("SELECT COALESCE(MAX(c.order), 0) FROM Category c WHERE c.deleted = false")
    fun findMaxOrder(): Long
}

@Repository
interface ProductRepository : BaseRepository<Product> {

}

@Repository
interface UserRepository : BaseRepository<User> {
    fun existsByUsernameAndDeletedIsFalse(username: String): Boolean
    fun findByUsername(username: String): User?
    fun findByUsernameAndDeletedFalse(username: String): User?
}

@Repository
interface UserPaymentTransactionRepository : BaseRepository<UserPaymentTransaction> {
    @Query("""
        SELECT p FROM UserPaymentTransaction p 
        WHERE p.user.id = :userId AND p.deleted = false 
        ORDER BY p.createdDate DESC
    """)
    fun findByUserIdAndDeletedFalseOrderByCreatedDateDesc(
        userId: Long,
        pageable: Pageable
    ): Page<UserPaymentTransaction>
}

@Repository
interface TransactionRepository : BaseRepository<Transaction> {

    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.user.id = :userId AND t.deleted = false 
        ORDER BY t.createdDate DESC
    """)
    fun findByUserIdAndDeletedFalseOrderByCreatedDateDesc(
        userId: Long,
        pageable: Pageable
    ): Page<Transaction>

    @Query("""
        SELECT t FROM Transaction t 
        WHERE t.deleted = false 
        ORDER BY t.createdDate DESC
    """)
    fun findAllByDeletedFalseOrderByCreatedDateDesc(
        pageable: Pageable
    ): Page<Transaction>
}