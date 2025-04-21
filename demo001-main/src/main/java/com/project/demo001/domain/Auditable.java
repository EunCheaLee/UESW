package com.project.demo001.domain;

import java.time.LocalDateTime;

public interface Auditable {
	void setCreateAt(LocalDateTime createAt);
	void setLastConnect(LocalDateTime lastConnect);
}

// 마이크테스트
