package com.study.myjdbc.repository;

import com.study.myjdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class MemberRepositoryVOTest {

    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        Member member = new Member("MemberV2", 10000);
        repository.save(member);
    }

}
