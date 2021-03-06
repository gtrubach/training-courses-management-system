package org.exadel.training.dao;

import org.exadel.training.model.Training;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class TrainingDAOImpl implements TrainingDAO {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void addTraining(Training training) {
        if (training != null) {
            sessionFactory.getCurrentSession().persist(training);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getAllTrainings() {
        Collection result = new LinkedHashSet(sessionFactory.getCurrentSession().createCriteria(Training.class).list());
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getFutureTrainingsForScheduling() {
        Collection result = new LinkedHashSet(sessionFactory.getCurrentSession().createQuery("FROM Training t WHERE t.date >= current_timestamp and (t.isApproved = true or t.isApproved = null)").list());
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getFutureTrainingsForRecommendation() {
        Collection result = new LinkedHashSet(sessionFactory.getCurrentSession().createQuery("FROM Training t WHERE (t.date >= current_timestamp or t.end >= current_date )and (t.isApproved = true or t.isApproved = null)").list());
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getTrainingsByName(String name) {
        Collection result = new LinkedHashSet(sessionFactory.getCurrentSession().createCriteria(Training.class)
                .add(Restrictions.eq("title", name)).list());
        return new ArrayList<>(result);
    }

    @Override
    public void updateTraining(Training training) {
        if (training != null) {
            sessionFactory.getCurrentSession().update(training);
        }
    }

    @Override
    public void removeTrainingById(long id) {
        Training training = getTrainingById(id);
        if (training != null) {
            sessionFactory.getCurrentSession().delete(training);
        }
    }

    @Override
    public Training getTrainingById(long id) {
        return (Training) sessionFactory.getCurrentSession().get(Training.class, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getTrainingsByTrainer(long id) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class);
        criteria.add(Restrictions.eq("trainer.id", id));
        Collection result = new LinkedHashSet(criteria.list());
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getTrainingsByVisitor(long id) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class);
        criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN).add(Restrictions.eq("visitorsAlias.userId", id));
        Collection result = new LinkedHashSet(criteria.list());
        return new ArrayList<>(result);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getSomeTrainingOrderBy(String person, String come, int pageNum, int pageSize, String sorting, String order, boolean admin) {
        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")));
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("trainer.userId", id),
                            Restrictions.eq("visitorsAlias.userId", id)
                    ));
        }
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }
        if (!sorting.equals("coach")) {
            if (order.equals("asc")) {
                criteria.addOrder(Order.asc(sorting));
            } else {
                criteria.addOrder(Order.desc(sorting));
            }
        } else {
            criteria.createAlias("trainer", "t");
            if (order.equals("asc")) {
                criteria.addOrder(Order.asc("t.firstName"))
                        .addOrder(Order.asc("t.lastName"));
            } else {
                criteria.addOrder(Order.desc("t.firstName"))
                        .addOrder(Order.desc("t.lastName"));
            }
        }
        if (!admin) {
            criteria.add(Restrictions.or(Restrictions.eq("isApproved", true),
                    Restrictions.eq("isEditing", true)));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        int size = criteria.list().size();
        List<Long> id = criteria.setFirstResult((pageNum - 1) * pageSize)
                .setMaxResults(pageSize)
                .list();
        List<Training> result = new ArrayList<>(id.size());
        result.addAll(id.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(2);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByTitle(String person, String come, boolean isAdmin, int pageNumber, int pageSize, String value) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .add(Restrictions.like("title", "%" + value + "%"));
        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }
        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        Integer size = criteria.list().size();
        List<Long> idList = criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize).list();
        List<Training> result = new ArrayList<>(idList.size());
        result.addAll(idList.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(2);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByDate(String person, String come, boolean isAdmin, int pageNumber, int pageSize, Timestamp date) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .add(Restrictions.eq("date", date));
        Timestamp sortDate = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", sortDate),
                    Restrictions.ge("end", sortDate)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", sortDate),
                    Restrictions.le("end", sortDate)
            ));
        }
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }
        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        Integer size = criteria.list().size();
        List<Long> idList = criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize).list();
        List<Training> result = new ArrayList<>(idList.size());
        result.addAll(idList.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(0);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByTime(String person, String come, boolean isAdmin, int pageNumber, int pageSize, String time) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .add(Restrictions.like("time", time + "%"));
        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }
        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        Integer size = criteria.list().size();
        List<Long> idList = criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize).list();
        List<Training> result = new ArrayList<>(idList.size());
        result.addAll(idList.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(0);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByLocation(String person, String come, boolean isAdmin, int pageNumber, int pageSize, String location) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .add(Restrictions.like("location", "%" + location + "%"));
        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }
        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        Integer size = criteria.list().size();
        List<Long> idList = criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize).list();
        List<Training> result = new ArrayList<>(idList.size());
        result.addAll(idList.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(0);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByTrainerName(String person, String come, boolean isAdmin, int pageNumber, int pageSize, String firstName, String lastName) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .createAlias("trainer", "t")
                .add(Restrictions.or(
                        Restrictions.and(
                                Restrictions.like("t.firstName", "%" + firstName + "%"),
                                Restrictions.like("t.lastName", "%" + lastName + "%")
                        ),
                        Restrictions.and(
                                Restrictions.like("t.firstName", "%" + lastName + "%"),
                                Restrictions.like("t.lastName", "%" + firstName + "%")
                        )
                ));
        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }
        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }
        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }
        Integer size = criteria.list().size();
        List<Long> idList = criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize).list();
        List<Training> result = new ArrayList<>(idList.size());
        result.addAll(idList.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> map = new HashMap<>(0);
        map.put("size", size);
        map.put("list", result);
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> searchTrainingsByTags(String person, String come, boolean isAdmin, int pageNumber, int pageSize, String[] values) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Training.class)
                .setProjection(Projections.distinct(Projections.property("trainingId")))
                .createAlias("tags", "tag");
        Disjunction disjunction = Restrictions.disjunction();
        for (String string : values) {
            disjunction.add(Restrictions.like("tag.name", '%' + string + '%'));
        }
        criteria.add(disjunction);

        Timestamp date = (Timestamp) sessionFactory.getCurrentSession().createSQLQuery("SELECT CURRENT_TIMESTAMP").list().get(0);
        if (come.equals("future")) {
            criteria.add(Restrictions.or(
                    Restrictions.ge("date", date),
                    Restrictions.ge("end", date)
            ));
        } else if (come.equals("past")) {
            criteria.add(Restrictions.or(
                    Restrictions.le("date", date),
                    Restrictions.le("end", date)
            ));
        }

        if (!person.equals("all")) {
            long id = Long.parseLong(person);
            criteria.createAlias("visitors", "visitorsAlias", JoinType.LEFT_OUTER_JOIN)
                    .add(Restrictions.or(
                            Restrictions.eq("visitorsAlias.userId", id),
                            Restrictions.eq("trainer.userId", id)
                    ));
        }

        if (!isAdmin) {
            criteria.add(Restrictions.eq("isApproved", true));
        } else {
            criteria.add(Restrictions.or(
                    Restrictions.eq("isApproved", true),
                    Restrictions.isNull("isApproved")
            ));
        }

        int size = criteria.list().size();
        criteria.setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize);
        List<Long> list = criteria.list();
        List<Training> users = new ArrayList<>(list.size());
        users.addAll(list.stream().map(this::getTrainingById).collect(Collectors.toList()));
        Map<String, Object> result = new HashMap<>(2);
        result.put("size", size);
        result.put("list", users);
        return result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<Training> getContinuousTrainings(long id) {
        Training training = getTrainingById(id);
        String title = training.getTitle();
        String name = title.substring(0, title.lastIndexOf("#"));
        return sessionFactory.getCurrentSession().createCriteria(Training.class)
                .add(Restrictions.like("title", name + "#%"))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("title"))
                .list();
    }
}
