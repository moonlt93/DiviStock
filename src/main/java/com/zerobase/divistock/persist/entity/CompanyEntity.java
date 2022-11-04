package com.zerobase.divistock.persist.entity;

import com.zerobase.divistock.model.Company;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Getter
@ToString
@NoArgsConstructor //arg하나도 없는 생성자 생성
@Entity(name="COMPANY")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String ticker;

    private String name;



    public  CompanyEntity(Company company){
        this.ticker= company.getTicker();
        this.name= company.getName();
    }





}
