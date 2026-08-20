package com.ems.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @Column(name = "location_code", length = 6, columnDefinition = "char(6)")
    private String locationCode;

    @Column(name = "department_code", length = 50)
    private String departmentCode;

    @Column(name = "province_code", length = 50)
    private String provinceCode;

    @Column(name = "district_code", length = 50)
    private String districtCode;

    @Column(name = "department", nullable = false, length = 30)
    private String department;

    @Column(name = "province", nullable = false, length = 30)
    private String province;

    @Column(name = "district", nullable = false, length = 50)
    private String district;

   //Getters y Setters

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}
