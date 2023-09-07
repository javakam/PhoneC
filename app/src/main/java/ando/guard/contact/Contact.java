package ando.guard.contact;

public class Contact {
    private long id; // 联系人ID
    private String displayName; // 姓名
    private String givenName; // 名字
    private String familyName; // 姓氏
    private String middleName; // 中间名
    private String suffix; // 后缀
    private String prefix; // 前缀
    private String nickname; // 昵称
    private String company; // 公司名称
    private String jobTitle; // 职位
    private String email; // 电子邮件地址
    private String website; // 网站地址
    private String notes; // 备注
    private String photoUri; // 头像URI
    private int type; // 联系人类型
    private long lastTimeContacted; // 上次通话时间
    private int timesContacted; // 通话次数
    private boolean starred; // 是否加星标
    private boolean hasPhoneNumber; // 是否有电话号码
    private String phoneNumber; // 电话号码
    private int phoneType; // 号码类型
    private String phoneLabel; // 号码标签
    private String address; // 地址
    private int addressType; // 地址类型
    private String addressLabel; // 地址标签
    private String birthday; // 生日

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getLastTimeContacted() {
        return lastTimeContacted;
    }

    public void setLastTimeContacted(long lastTimeContacted) {
        this.lastTimeContacted = lastTimeContacted;
    }

    public int getTimesContacted() {
        return timesContacted;
    }

    public void setTimesContacted(int timesContacted) {
        this.timesContacted = timesContacted;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public boolean isHasPhoneNumber() {
        return hasPhoneNumber;
    }

    public void setHasPhoneNumber(boolean hasPhoneNumber) {
        this.hasPhoneNumber = hasPhoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(int phoneType) {
        this.phoneType = phoneType;
    }

    public String getPhoneLabel() {
        return phoneLabel;
    }

    public void setPhoneLabel(String phoneLabel) {
        this.phoneLabel = phoneLabel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
