package com.only4.cap4k.reference.contentstudio.domain._share.meta.paid_publication_task

import com.only4.cap4k.ddd.domain.repo.JpaPredicate
import com.only4.cap4k.ddd.domain.repo.schema.ExpressionBuilder
import com.only4.cap4k.ddd.domain.repo.schema.Field
import com.only4.cap4k.ddd.domain.repo.schema.OrderBuilder
import com.only4.cap4k.ddd.domain.repo.schema.PredicateBuilder
import com.only4.cap4k.ddd.domain.repo.schema.SchemaSpecification
import com.only4.cap4k.ddd.domain.repo.schema.SubqueryConfigure
import com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.PaidPublicationTask
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Subquery
import java.util.UUID
import org.springframework.data.jpa.domain.Specification

class SPaidPublicationTask(
    private val root: Path<PaidPublicationTask>,
    private val criteriaBuilder: CriteriaBuilder,
) {
    class PROPERTY_NAMES {

        val id = "id"

        val contentId = "contentId"

        val paidPublicationStatus = "paidPublicationStatus"

        val publicationSagaId = "publicationSagaId"

        val payoutHoldStatus = "payoutHoldStatus"

        val payoutHoldId = "payoutHoldId"

        val entitlementPlanStatus = "entitlementPlanStatus"

        val entitlementPlanId = "entitlementPlanId"

        val startedAt = "startedAt"

        val publishedAt = "publishedAt"

        val completedAt = "completedAt"

        val failedAt = "failedAt"

        val failedReason = "failedReason"

        val dbCreatedAt = "dbCreatedAt"

        val dbUpdatedAt = "dbUpdatedAt"

    }

    companion object {

        val props = PROPERTY_NAMES()

        @JvmStatic
        fun specify(builder: PredicateBuilder<SPaidPublicationTask>): Specification<PaidPublicationTask> {
            return specify(builder, false, emptyList())
        }

        @JvmStatic
        fun specify(builder: PredicateBuilder<SPaidPublicationTask>, distinct: Boolean): Specification<PaidPublicationTask> {
            return specify(builder, distinct, emptyList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPaidPublicationTask>,
            vararg orderBuilders: OrderBuilder<SPaidPublicationTask>,
        ): Specification<PaidPublicationTask> {
            return specify(builder, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPaidPublicationTask>,
            orderBuilders: List<OrderBuilder<SPaidPublicationTask>>,
        ): Specification<PaidPublicationTask> {
            return specify(builder, false, orderBuilders)
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPaidPublicationTask>,
            distinct: Boolean,
            vararg orderBuilders: OrderBuilder<SPaidPublicationTask>,
        ): Specification<PaidPublicationTask> {
            return specify(builder, distinct, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPaidPublicationTask>,
            distinct: Boolean,
            orderBuilders: List<OrderBuilder<SPaidPublicationTask>>,
        ): Specification<PaidPublicationTask> {
            return specify { schema, criteriaQuery, _ ->
                criteriaQuery.where(builder.build(schema))
                criteriaQuery.distinct(distinct)
                if (orderBuilders.isNotEmpty()) {
                    criteriaQuery.orderBy(orderBuilders.map { it.build(schema) })
                }
                null
            }
        }

        @JvmStatic
        fun specify(specifier: SchemaSpecification<PaidPublicationTask, SPaidPublicationTask>): Specification<PaidPublicationTask> {
            return Specification { root, criteriaQuery, criteriaBuilder ->
                val schema = SPaidPublicationTask(root, criteriaBuilder)
                specifier.toPredicate(schema, criteriaQuery, criteriaBuilder)
            }
        }

        @JvmStatic
        fun <E> subquery(
            resultClass: Class<E>,
            selectBuilder: ExpressionBuilder<SPaidPublicationTask, E>,
            predicateBuilder: PredicateBuilder<SPaidPublicationTask>,
            criteriaBuilder: CriteriaBuilder,
            criteriaQuery: CriteriaQuery<*>,
        ): Subquery<E> {
            return subquery(resultClass, { sq, schema ->
                sq.select(selectBuilder.build(schema))
                sq.where(predicateBuilder.build(schema))
            }, criteriaBuilder, criteriaQuery)
        }

        @JvmStatic
        fun <E> subquery(
            resultClass: Class<E>,
            subqueryConfigure: SubqueryConfigure<E, SPaidPublicationTask>,
            criteriaBuilder: CriteriaBuilder,
            criteriaQuery: CriteriaQuery<*>,
        ): Subquery<E> {
            val sq = criteriaQuery.subquery(resultClass)
            val root = sq.from(PaidPublicationTask::class.java)
            val schema = SPaidPublicationTask(root, criteriaBuilder)
            subqueryConfigure.configure(sq, schema)
            return sq
        }

        @JvmStatic
        fun predicateById(id: Any): JpaPredicate<PaidPublicationTask> {
            return JpaPredicate.byId(PaidPublicationTask::class.java, id)
        }

        @JvmStatic
        fun predicateByIds(ids: Iterable<*>): JpaPredicate<PaidPublicationTask> {
            @Suppress("UNCHECKED_CAST")
            return JpaPredicate.byIds(PaidPublicationTask::class.java, ids as Iterable<Any>)
        }

        @JvmStatic
        fun predicateByIds(vararg ids: Any): JpaPredicate<PaidPublicationTask> {
            return JpaPredicate.byIds(PaidPublicationTask::class.java, ids.toList())
        }

        @JvmStatic
        fun predicate(builder: PredicateBuilder<SPaidPublicationTask>): JpaPredicate<PaidPublicationTask> {
            return JpaPredicate.bySpecification(PaidPublicationTask::class.java, specify(builder))
        }

        @JvmStatic
        fun predicate(specifier: SchemaSpecification<PaidPublicationTask, SPaidPublicationTask>): JpaPredicate<PaidPublicationTask> {
            return JpaPredicate.bySpecification(PaidPublicationTask::class.java, specify(specifier))
        }
    }

    fun _criteriaBuilder(): CriteriaBuilder = criteriaBuilder

    fun _root(): Path<PaidPublicationTask> = root

    val id: Field<UUID> by lazy {
        Field(root.get("id"), criteriaBuilder)
    }

    val contentId: Field<UUID> by lazy {
        Field(root.get("contentId"), criteriaBuilder)
    }

    val paidPublicationStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PaidPublicationStatus> by lazy {
        Field(root.get("paidPublicationStatus"), criteriaBuilder)
    }

    val publicationSagaId: Field<String> by lazy {
        Field(root.get("publicationSagaId"), criteriaBuilder)
    }

    val payoutHoldStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.PayoutHoldStatus> by lazy {
        Field(root.get("payoutHoldStatus"), criteriaBuilder)
    }

    val payoutHoldId: Field<String> by lazy {
        Field(root.get("payoutHoldId"), criteriaBuilder)
    }

    val entitlementPlanStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.paid_publication_task.enums.EntitlementPlanStatus> by lazy {
        Field(root.get("entitlementPlanStatus"), criteriaBuilder)
    }

    val entitlementPlanId: Field<String> by lazy {
        Field(root.get("entitlementPlanId"), criteriaBuilder)
    }

    val startedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("startedAt"), criteriaBuilder)
    }

    val publishedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("publishedAt"), criteriaBuilder)
    }

    val completedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("completedAt"), criteriaBuilder)
    }

    val failedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("failedAt"), criteriaBuilder)
    }

    val failedReason: Field<String> by lazy {
        Field(root.get("failedReason"), criteriaBuilder)
    }

    val dbCreatedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("dbCreatedAt"), criteriaBuilder)
    }

    val dbUpdatedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("dbUpdatedAt"), criteriaBuilder)
    }

    fun all(vararg restrictions: Predicate): Predicate {
        return criteriaBuilder.and(*restrictions)
    }

    fun any(vararg restrictions: Predicate): Predicate {
        return criteriaBuilder.or(*restrictions)
    }

    fun allNotNull(vararg restrictions: Predicate?): Predicate? {
        val nonNullRestrictions = restrictions.filterNotNull().toTypedArray()
        return when {
            nonNullRestrictions.isEmpty() -> null
            nonNullRestrictions.size == 1 -> nonNullRestrictions[0]
            else -> criteriaBuilder.and(*nonNullRestrictions)
        }
    }

    fun anyNotNull(vararg restrictions: Predicate?): Predicate? {
        val nonNullRestrictions = restrictions.filterNotNull().toTypedArray()
        return when {
            nonNullRestrictions.isEmpty() -> null
            nonNullRestrictions.size == 1 -> nonNullRestrictions[0]
            else -> criteriaBuilder.or(*nonNullRestrictions)
        }
    }

    fun not(restriction: Predicate): Predicate = criteriaBuilder.not(restriction)

    fun spec(builder: PredicateBuilder<SPaidPublicationTask>): Predicate {
        return builder.build(this)
    }
}
