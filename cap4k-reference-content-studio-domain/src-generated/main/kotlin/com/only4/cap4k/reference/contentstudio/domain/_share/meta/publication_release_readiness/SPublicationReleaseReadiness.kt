package com.only4.cap4k.reference.contentstudio.domain._share.meta.publication_release_readiness

import com.only4.cap4k.ddd.domain.repo.JpaPredicate
import com.only4.cap4k.ddd.domain.repo.schema.ExpressionBuilder
import com.only4.cap4k.ddd.domain.repo.schema.Field
import com.only4.cap4k.ddd.domain.repo.schema.OrderBuilder
import com.only4.cap4k.ddd.domain.repo.schema.PredicateBuilder
import com.only4.cap4k.ddd.domain.repo.schema.SchemaSpecification
import com.only4.cap4k.ddd.domain.repo.schema.SubqueryConfigure
import com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.PublicationReleaseReadiness
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Subquery
import java.util.UUID
import org.springframework.data.jpa.domain.Specification

class SPublicationReleaseReadiness(
    private val root: Path<PublicationReleaseReadiness>,
    private val criteriaBuilder: CriteriaBuilder,
) {
    class PROPERTY_NAMES {

        val id = "id"

        val contentId = "contentId"

        val mediaProcessingTaskId = "mediaProcessingTaskId"

        val readinessState = "readinessState"

        val copyrightStatus = "copyrightStatus"

        val manualConfirmationStatus = "manualConfirmationStatus"

        val releaseWindowOpensAt = "releaseWindowOpensAt"

        val releaseWindowClosesAt = "releaseWindowClosesAt"

        val readyAt = "readyAt"

        val cancelReason = "cancelReason"

        val dbCreatedAt = "dbCreatedAt"

        val dbUpdatedAt = "dbUpdatedAt"

    }

    companion object {

        val props = PROPERTY_NAMES()

        @JvmStatic
        fun specify(builder: PredicateBuilder<SPublicationReleaseReadiness>): Specification<PublicationReleaseReadiness> {
            return specify(builder, false, emptyList())
        }

        @JvmStatic
        fun specify(builder: PredicateBuilder<SPublicationReleaseReadiness>, distinct: Boolean): Specification<PublicationReleaseReadiness> {
            return specify(builder, distinct, emptyList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPublicationReleaseReadiness>,
            vararg orderBuilders: OrderBuilder<SPublicationReleaseReadiness>,
        ): Specification<PublicationReleaseReadiness> {
            return specify(builder, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPublicationReleaseReadiness>,
            orderBuilders: List<OrderBuilder<SPublicationReleaseReadiness>>,
        ): Specification<PublicationReleaseReadiness> {
            return specify(builder, false, orderBuilders)
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPublicationReleaseReadiness>,
            distinct: Boolean,
            vararg orderBuilders: OrderBuilder<SPublicationReleaseReadiness>,
        ): Specification<PublicationReleaseReadiness> {
            return specify(builder, distinct, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SPublicationReleaseReadiness>,
            distinct: Boolean,
            orderBuilders: List<OrderBuilder<SPublicationReleaseReadiness>>,
        ): Specification<PublicationReleaseReadiness> {
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
        fun specify(specifier: SchemaSpecification<PublicationReleaseReadiness, SPublicationReleaseReadiness>): Specification<PublicationReleaseReadiness> {
            return Specification { root, criteriaQuery, criteriaBuilder ->
                val schema = SPublicationReleaseReadiness(root, criteriaBuilder)
                specifier.toPredicate(schema, criteriaQuery, criteriaBuilder)
            }
        }

        @JvmStatic
        fun <E> subquery(
            resultClass: Class<E>,
            selectBuilder: ExpressionBuilder<SPublicationReleaseReadiness, E>,
            predicateBuilder: PredicateBuilder<SPublicationReleaseReadiness>,
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
            subqueryConfigure: SubqueryConfigure<E, SPublicationReleaseReadiness>,
            criteriaBuilder: CriteriaBuilder,
            criteriaQuery: CriteriaQuery<*>,
        ): Subquery<E> {
            val sq = criteriaQuery.subquery(resultClass)
            val root = sq.from(PublicationReleaseReadiness::class.java)
            val schema = SPublicationReleaseReadiness(root, criteriaBuilder)
            subqueryConfigure.configure(sq, schema)
            return sq
        }

        @JvmStatic
        fun predicateById(id: Any): JpaPredicate<PublicationReleaseReadiness> {
            return JpaPredicate.byId(PublicationReleaseReadiness::class.java, id)
        }

        @JvmStatic
        fun predicateByIds(ids: Iterable<*>): JpaPredicate<PublicationReleaseReadiness> {
            @Suppress("UNCHECKED_CAST")
            return JpaPredicate.byIds(PublicationReleaseReadiness::class.java, ids as Iterable<Any>)
        }

        @JvmStatic
        fun predicateByIds(vararg ids: Any): JpaPredicate<PublicationReleaseReadiness> {
            return JpaPredicate.byIds(PublicationReleaseReadiness::class.java, ids.toList())
        }

        @JvmStatic
        fun predicate(builder: PredicateBuilder<SPublicationReleaseReadiness>): JpaPredicate<PublicationReleaseReadiness> {
            return JpaPredicate.bySpecification(PublicationReleaseReadiness::class.java, specify(builder))
        }

        @JvmStatic
        fun predicate(specifier: SchemaSpecification<PublicationReleaseReadiness, SPublicationReleaseReadiness>): JpaPredicate<PublicationReleaseReadiness> {
            return JpaPredicate.bySpecification(PublicationReleaseReadiness::class.java, specify(specifier))
        }
    }

    fun _criteriaBuilder(): CriteriaBuilder = criteriaBuilder

    fun _root(): Path<PublicationReleaseReadiness> = root

    val id: Field<UUID> by lazy {
        Field(root.get("id"), criteriaBuilder)
    }

    val contentId: Field<UUID> by lazy {
        Field(root.get("contentId"), criteriaBuilder)
    }

    val mediaProcessingTaskId: Field<UUID> by lazy {
        Field(root.get("mediaProcessingTaskId"), criteriaBuilder)
    }

    val readinessState: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.PublicationReleaseReadinessState> by lazy {
        Field(root.get("readinessState"), criteriaBuilder)
    }

    val copyrightStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.CopyrightReviewStatus> by lazy {
        Field(root.get("copyrightStatus"), criteriaBuilder)
    }

    val manualConfirmationStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.publication_release_readiness.enums.ManualReleaseConfirmationStatus> by lazy {
        Field(root.get("manualConfirmationStatus"), criteriaBuilder)
    }

    val releaseWindowOpensAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("releaseWindowOpensAt"), criteriaBuilder)
    }

    val releaseWindowClosesAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("releaseWindowClosesAt"), criteriaBuilder)
    }

    val readyAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("readyAt"), criteriaBuilder)
    }

    val cancelReason: Field<String> by lazy {
        Field(root.get("cancelReason"), criteriaBuilder)
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

    fun spec(builder: PredicateBuilder<SPublicationReleaseReadiness>): Predicate {
        return builder.build(this)
    }
}
