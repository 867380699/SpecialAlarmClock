package zeusro.specialalarmclock.bean;

import zeusro.specialalarmclock.Key;
import zeusro.specialalarmclock.Type;

/**
 *
 * Created by Z on 2015/11/16.
 */
public class AlarmBean {

    private Key key;
    private String title;
    private String summary;
    private Object value;
    private String[] options;
    private Type type;

    public AlarmBean(Key key, Object value, Type type) {
        this(key,null,null,null, value, type);
    }

    public AlarmBean(Key key, String title, String summary, String[] options, Object value, Type type) {
        this.key = key;
        this.title = title;
        this.summary = summary;
        this.value = value;
        this.options = options;
        this.type = type;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
