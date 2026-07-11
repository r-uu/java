package de.ruu.lib.jpa.core.criteria;

/**
 * Order used for manipulating order by clauses.
 *
 * @author Maciej Szczytowski (mszczytowski-genericdao@gmail.com)
 * @since 1.0
 */
public class Order<T>
{
    private boolean ascending;
    private String property;

    private Order(String property, boolean ascending)
    {
        this.property  = property;
        this.ascending = ascending;
    }

    /**
     * Generate part of EQL order by clause with given criteria.
     *
     * @param criteria criteria used in order
     * @param criteriaQuery current query
     * @return part of order by clause
     */
    public String toSqlString(Criteria<T> criteria, Criteria<T>.CriteriaQuery criteriaQuery)
    {
        return criteriaQuery.propertyName(property, criteria) + (ascending ? " asc" : " desc");
    }

    /**
     * Create new ascending order with given property.
     *
     * @param property property used in order
     * @return new query object
     */
    public static Order<?> asc(String property) {
        return new Order<>(property, true);
    }

    /**
     * Create new descending order with given property.
     *
     * @param property property used in order
     * @return new query object
     */
    public static Order<?> desc(String property) {
        return new Order<>(property, false);
    }
}