package com.github.jochenw.afw.lc.ht;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="WBETenants",
       uniqueConstraints={@UniqueConstraint(name="UX_Tenants_Name", columnNames="name")})
public class Tenant {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	@Column(length=32)
	private String name;
}
