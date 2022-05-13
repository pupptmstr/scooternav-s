package com.pupptmstr.scooternav_s.database

import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory

class Neo4jSessionFactory() {
    private val config = Configuration.Builder().credentials("neo4j", "test").uri("bolt://localhost:4112").connectionPoolSize(50).build()
    private val sessionFactory: SessionFactory = SessionFactory(config, "com.pupptmstr.scooternav_s.ogm")

    fun getNeo4jSession(): Session? {
        return sessionFactory.openSession()
    }
}
