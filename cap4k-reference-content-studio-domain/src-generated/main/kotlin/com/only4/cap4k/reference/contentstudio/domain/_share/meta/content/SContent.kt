package com.only4.cap4k.reference.contentstudio.domain._share.meta.content

import com.only4.cap4k.ddd.domain.repo.JpaPredicate
import com.only4.cap4k.ddd.domain.repo.schema.ExpressionBuilder
import com.only4.cap4k.ddd.domain.repo.schema.Field
import com.only4.cap4k.ddd.domain.repo.schema.OrderBuilder
import com.only4.cap4k.ddd.domain.repo.schema.PredicateBuilder
import com.only4.cap4k.ddd.domain.repo.schema.SchemaSpecification
import com.only4.cap4k.ddd.domain.repo.schema.SubqueryConfigure
import com.only4.cap4k.reference.contentstudio.domain.aggregates.content.Content
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Subquery
import java.util.UUID
import org.springframework.data.jpa.domain.Specification

class SContent(
    private val root: Path<Content>,
    private val criteriaBuilder: CriteriaBuilder,
) {
    class PROPERTY_NAMES {

        val id = "id"

        val title = "title"

        val body = "body"

        val mediaSourceKey = "mediaSourceKey"

        val reviewStatus = "reviewStatus"

        val contentStatus = "contentStatus"

        val reviewerId = "reviewerId"

        val reviewedAt = "reviewedAt"

        val publishedAt = "publishedAt"

        val dbCreatedAt = "dbCreatedAt"

        val dbUpdatedAt = "dbUpdatedAt"

    }

    companion object {

        val props = PROPERTY_NAMES()

        @JvmStatic
        fun specify(builder: PredicateBuilder<SContent>): Specification<Content> {
            return specify(builder, false, emptyList())
        }

        @JvmStatic
        fun specify(builder: PredicateBuilder<SContent>, distinct: Boolean): Specification<Content> {
            return specify(builder, distinct, emptyList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SContent>,
            vararg orderBuilders: OrderBuilder<SContent>,
        ): Specification<Content> {
            return specify(builder, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SContent>,
            orderBuilders: List<OrderBuilder<SContent>>,
        ): Specification<Content> {
            return specify(builder, false, orderBuilders)
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SContent>,
            distinct: Boolean,
            vararg orderBuilders: OrderBuilder<SContent>,
        ): Specification<Content> {
            return specify(builder, distinct, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SContent>,
            distinct: Boolean,
            orderBuilders: List<OrderBuilder<SContent>>,
        ): Specification<Content> {
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
        fun specify(specifier: SchemaSpecification<Content, SContent>): Specification<Content> {
            return Specification { root, criteriaQuery, criteriaBuilder ->
                val schema = SContent(root, criteriaBuilder)
                specifier.toPredicate(schema, criteriaQuery, criteriaBuilder)
            }
        }

        @JvmStatic
        fun <E> subquery(
            resultClass: Class<E>,
            selectBuilder: ExpressionBuilder<SContent, E>,
            predicateBuilder: PredicateBuilder<SContent>,
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
            subqueryConfigure: SubqueryConfigure<E, SContent>,
            criteriaBuilder: CriteriaBuilder,
            criteriaQuery: CriteriaQuery<*>,
        ): Subquery<E> {
            val sq = criteriaQuery.subquery(resultClass)
            val root = sq.from(Content::class.java)
            val schema = SContent(root, criteriaBuilder)
            subqueryConfigure.configure(sq, schema)
            return sq
        }

        @JvmStatic
        fun predicateById(id: Any): JpaPredicate<Content> {
            return JpaPredicate.byId(Content::class.java, id)
        }

        @JvmStatic
        fun predicateByIds(ids: Iterable<*>): JpaPredicate<Content> {
            @Suppress("UNCHECKED_CAST")
            return JpaPredicate.byIds(Content::class.java, ids as Iterable<Any>)
        }

        @JvmStatic
        fun predicateByIds(vararg ids: Any): JpaPredicate<Content> {
            return JpaPredicate.byIds(Content::class.java, ids.toList())
        }

        @JvmStatic
        fun predicate(builder: PredicateBuilder<SContent>): JpaPredicate<Content> {
            return JpaPredicate.bySpecification(Content::class.java, specify(builder))
        }

        @JvmStatic
        fun predicate(specifier: SchemaSpecification<Content, SContent>): JpaPredicate<Content> {
            return JpaPredicate.bySpecification(Content::class.java, specify(specifier))
        }
    }

    fun _criteriaBuilder(): CriteriaBuilder = criteriaBuilder

    fun _root(): Path<Content> = root

    val id: Field<UUID> by lazy {
        Field(root.get("id"), criteriaBuilder)
    }

    val title: Field<String> by lazy {
        Field(root.get("title"), criteriaBuilder)
    }

    val body: Field<String> by lazy {
        Field(root.get("body"), criteriaBuilder)
    }

    val mediaSourceKey: Field<String> by lazy {
        Field(root.get("mediaSourceKey"), criteriaBuilder)
    }

    val reviewStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ReviewStatus> by lazy {
        Field(root.get("reviewStatus"), criteriaBuilder)
    }

    val contentStatus: Field<com.only4.cap4k.reference.contentstudio.domain.aggregates.content.enums.ContentStatus> by lazy {
        Field(root.get("contentStatus"), criteriaBuilder)
    }

    val reviewerId: Field<UUID> by lazy {
        Field(root.get("reviewerId"), criteriaBuilder)
    }

    val reviewedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("reviewedAt"), criteriaBuilder)
    }

    val publishedAt: Field<java.time.LocalDateTime> by lazy {
        Field(root.get("publishedAt"), criteriaBuilder)
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

    fun spec(builder: PredicateBuilder<SContent>): Predicate {
        return builder.build(this)
    }
}
