package com.only4.cap4k.reference.contentstudio.domain._share.meta.media_processing_task

import com.only4.cap4k.ddd.domain.repo.JpaPredicate
import com.only4.cap4k.ddd.domain.repo.schema.ExpressionBuilder
import com.only4.cap4k.ddd.domain.repo.schema.Field
import com.only4.cap4k.ddd.domain.repo.schema.OrderBuilder
import com.only4.cap4k.ddd.domain.repo.schema.PredicateBuilder
import com.only4.cap4k.ddd.domain.repo.schema.SchemaSpecification
import com.only4.cap4k.ddd.domain.repo.schema.SubqueryConfigure
import com.only4.cap4k.reference.contentstudio.domain.aggregates.media_processing_task.MediaProcessingTask
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Subquery
import java.util.UUID
import org.springframework.data.jpa.domain.Specification

class SMediaProcessingTask(
    private val root: Path<MediaProcessingTask>,
    private val criteriaBuilder: CriteriaBuilder,
) {
    class PROPERTY_NAMES {

        val id = "id"

        val contentId = "contentId"

        val externalTaskId = "externalTaskId"

        val processingStatus = "processingStatus"

        val dbCreatedAt = "dbCreatedAt"

        val dbUpdatedAt = "dbUpdatedAt"

    }

    companion object {

        val props = PROPERTY_NAMES()

        @JvmStatic
        fun specify(builder: PredicateBuilder<SMediaProcessingTask>): Specification<MediaProcessingTask> {
            return specify(builder, false, emptyList())
        }

        @JvmStatic
        fun specify(builder: PredicateBuilder<SMediaProcessingTask>, distinct: Boolean): Specification<MediaProcessingTask> {
            return specify(builder, distinct, emptyList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SMediaProcessingTask>,
            vararg orderBuilders: OrderBuilder<SMediaProcessingTask>,
        ): Specification<MediaProcessingTask> {
            return specify(builder, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SMediaProcessingTask>,
            orderBuilders: List<OrderBuilder<SMediaProcessingTask>>,
        ): Specification<MediaProcessingTask> {
            return specify(builder, false, orderBuilders)
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SMediaProcessingTask>,
            distinct: Boolean,
            vararg orderBuilders: OrderBuilder<SMediaProcessingTask>,
        ): Specification<MediaProcessingTask> {
            return specify(builder, distinct, orderBuilders.toList())
        }

        @JvmStatic
        fun specify(
            builder: PredicateBuilder<SMediaProcessingTask>,
            distinct: Boolean,
            orderBuilders: List<OrderBuilder<SMediaProcessingTask>>,
        ): Specification<MediaProcessingTask> {
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
        fun specify(specifier: SchemaSpecification<MediaProcessingTask, SMediaProcessingTask>): Specification<MediaProcessingTask> {
            return Specification { root, criteriaQuery, criteriaBuilder ->
                val schema = SMediaProcessingTask(root, criteriaBuilder)
                specifier.toPredicate(schema, criteriaQuery, criteriaBuilder)
            }
        }

        @JvmStatic
        fun <E> subquery(
            resultClass: Class<E>,
            selectBuilder: ExpressionBuilder<SMediaProcessingTask, E>,
            predicateBuilder: PredicateBuilder<SMediaProcessingTask>,
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
            subqueryConfigure: SubqueryConfigure<E, SMediaProcessingTask>,
            criteriaBuilder: CriteriaBuilder,
            criteriaQuery: CriteriaQuery<*>,
        ): Subquery<E> {
            val sq = criteriaQuery.subquery(resultClass)
            val root = sq.from(MediaProcessingTask::class.java)
            val schema = SMediaProcessingTask(root, criteriaBuilder)
            subqueryConfigure.configure(sq, schema)
            return sq
        }

        @JvmStatic
        fun predicateById(id: Any): JpaPredicate<MediaProcessingTask> {
            return JpaPredicate.byId(MediaProcessingTask::class.java, id)
        }

        @JvmStatic
        fun predicateByIds(ids: Iterable<*>): JpaPredicate<MediaProcessingTask> {
            @Suppress("UNCHECKED_CAST")
            return JpaPredicate.byIds(MediaProcessingTask::class.java, ids as Iterable<Any>)
        }

        @JvmStatic
        fun predicateByIds(vararg ids: Any): JpaPredicate<MediaProcessingTask> {
            return JpaPredicate.byIds(MediaProcessingTask::class.java, ids.toList())
        }

        @JvmStatic
        fun predicate(builder: PredicateBuilder<SMediaProcessingTask>): JpaPredicate<MediaProcessingTask> {
            return JpaPredicate.bySpecification(MediaProcessingTask::class.java, specify(builder))
        }

        @JvmStatic
        fun predicate(specifier: SchemaSpecification<MediaProcessingTask, SMediaProcessingTask>): JpaPredicate<MediaProcessingTask> {
            return JpaPredicate.bySpecification(MediaProcessingTask::class.java, specify(specifier))
        }
    }

    fun _criteriaBuilder(): CriteriaBuilder = criteriaBuilder

    fun _root(): Path<MediaProcessingTask> = root

    val id: Field<UUID> by lazy {
        Field(root.get("id"), criteriaBuilder)
    }

    val contentId: Field<UUID> by lazy {
        Field(root.get("contentId"), criteriaBuilder)
    }

    val externalTaskId: Field<String> by lazy {
        Field(root.get("externalTaskId"), criteriaBuilder)
    }

    val processingStatus: Field<String> by lazy {
        Field(root.get("processingStatus"), criteriaBuilder)
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

    fun spec(builder: PredicateBuilder<SMediaProcessingTask>): Predicate {
        return builder.build(this)
    }
}
