package pku.ss.cyy.bean;


public class City {
    private String province, city, number, firstPY, allPY, allFirstPY;

    public City(String province, String city, String number, String firstPY, String allPY, String allFirstPY) {
        this.province = province;
        this.city = city;
        this.number = number;
        this.firstPY = firstPY;
        this.allPY = allPY;
        this.allFirstPY = allFirstPY;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFirstPY() {
        return firstPY;
    }

    public void setFirstPY(String firstPY) {
        this.firstPY = firstPY;
    }

    public String getAllPY() {
        return allPY;
    }

    public void setAllPY(String allPY) {
        this.allPY = allPY;
    }

    public String getAllFirstPY() {
        return allFirstPY;
    }

    public void setAllFirstPY(String allFirstPY) {
        this.allFirstPY = allFirstPY;
    }
}
