package org.exadel.training.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

@Entity
@Table(name = "trainingFeedback")
public class EmployeeFeedback {

    @NotEmpty
    private long userId;

    @NotEmpty
    private long trainerId;

    @Column(length = 20)
    private String date;

    private boolean present;

    private int attitudeToLearning;

    private boolean askQuestions;

    private int communication;

    private boolean interest;

    private boolean focusedOnResults;

    @Column(length = 4500)
    private String text;

    private int level;

    @Column(length = 45)
    private String mark;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public int getAttitudeToLearning() {
        return attitudeToLearning;
    }

    public void setAttitudeToLearning(int attitudeToLearning) {
        this.attitudeToLearning = attitudeToLearning;
    }

    public boolean isAskQuestions() {
        return askQuestions;
    }

    public void setAskQuestions(boolean askQuestions) {
        this.askQuestions = askQuestions;
    }

    public int getCommunication() {
        return communication;
    }

    public void setCommunication(int communication) {
        this.communication = communication;
    }

    public boolean isInterest() {
        return interest;
    }

    public void setInterest(boolean interest) {
        this.interest = interest;
    }

    public boolean isFocusedOnResults() {
        return focusedOnResults;
    }

    public void setFocusedOnResults(boolean focusedOnResults) {
        this.focusedOnResults = focusedOnResults;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }
}
