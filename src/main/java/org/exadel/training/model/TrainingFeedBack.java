package org.exadel.training.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Entity
@Table(name = "trainingFeedback")
public class TrainingFeedback {

    @NotEmpty
    private long userId;

    @NotEmpty
    private long trainerId;

    @Column(length = 4500)
    private String text;

    @Column(length = 20)
    private String date;

    private int understandably;

    private int interestingly;

    private int learnSomethingNew;

    private int effectiveness;

    private boolean coachDesire;

    @Column(length = 45)
    private String recommending;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(long trainerId) {
        this.trainerId = trainerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getUnderstandably() {
        return understandably;
    }

    public void setUnderstandably(int understandably) {
        this.understandably = understandably;
    }

    public int getInterestingly() {
        return interestingly;
    }

    public void setInterestingly(int interestingly) {
        this.interestingly = interestingly;
    }

    public int getLearnSomethingNew() {
        return learnSomethingNew;
    }

    public void setLearnSomethingNew(int learnSomethingNew) {
        this.learnSomethingNew = learnSomethingNew;
    }

    public int getEffectiveness() {
        return effectiveness;
    }

    public void setEffectiveness(int effectiveness) {
        this.effectiveness = effectiveness;
    }

    public boolean isCoachDesire() {
        return coachDesire;
    }

    public void setCoachDesire(boolean coachDesire) {
        this.coachDesire = coachDesire;
    }

    public String getRecommending() {
        return recommending;
    }

    public void setRecommending(String recommending) {
        this.recommending = recommending;
    }
}
