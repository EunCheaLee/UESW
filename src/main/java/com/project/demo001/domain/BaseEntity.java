package com.project.demo001.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@EntityListeners(value = {AuditingEntityListener.class})
@MappedSuperclass
public class BaseEntity implements Auditable {

	@CreatedDate
	private LocalDateTime createAt;
	@LastModifiedDate
	private LocalDateTime lastConnect;
	@CreationTimestamp
	private LocalDateTime writeDate;
	
}
