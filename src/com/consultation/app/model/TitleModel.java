package com.consultation.app.model;

import java.util.List;

public class TitleModel {

    private String id = "0";

    private String name = "";

    private String level = "0";

    private String childCount = "0";

    private String title = "";
    
    private String isShow = "True";

    
    public String getIsShow() {
        return isShow;
    }

    
    public void setIsShow(String isShow) {
        this.isShow=isShow;
    }

    private List<ItemModel> itemModels;

    public String getChildCount() {
        return childCount;
    }

    public void setChildCount(String childCount) {
        this.childCount=childCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id=id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level=level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title=title;
    }

    public List<ItemModel> getItemModels() {
        return itemModels;
    }

    public void setItemModels(List<ItemModel> itemModels) {
        this.itemModels=itemModels;
    }

    public TitleModel() {
        super();
    }
}
