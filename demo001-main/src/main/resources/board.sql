/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.40 
*********************************************************************
*/
/*!40101 SET NAMES utf8 */;

CREATE TABLE board (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 글 고유 ID
    view_num INT DEFAULT 0,                                   -- 조회수
    reply_num INT DEFAULT 0,                                  -- 답글 수
    title VARCHAR(255) NOT NULL,                              -- 제목
    content TEXT NOT NULL,                                    -- 본문
    reply TEXT,                                               -- 답글 (댓글 아님)
    notice BOOLEAN DEFAULT FALSE,                    -- 공지사항 여부
    user_id BIGINT,                                           -- 유저 고유 ID
    create_at DATETIME DEFAULT CURRENT_TIMESTAMP,           -- 작성일자
    last_connect DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  -- 수정일자
);
INSERT INTO `board` (
    id, content, reply, title, reply_num, view_num, user_idx, user_name, password, create_date, update_date
) VALUES 
(1, '마이크테스트', NULL, '보드게시판입니다', 0, 0, NULL, NULL, NULL, NOW(), NOW()),
(2, '마이크테스트', NULL, '보드게시판입니다', 0, 0, NULL, NULL, NULL, NOW(), NOW()),
(3, '마이크테스트', NULL, '보드게시판입니다', 0, 0, NULL, NULL, NULL, NOW(), NOW());