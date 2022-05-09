package com.pupptmstr.scooternav_s

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values.parameters

class DatabaseConnector(uri: String, login: String, pass: String): AutoCloseable {
    private val driver = GraphDatabase.driver(uri, AuthTokens.basic(login, pass))

    fun printGreeting(message: String?) {
        driver.session().use { session ->
            val greeting: String = session.writeTransaction { tx ->
                val result = tx.run(
                    "CREATE (a:Greeting) " +
                            "SET a.message = \$message " +
                            "RETURN a.message + ', from node ' + id(a)",
                    parameters("message", message)
                )
                result.single().get(0).asString()
            }
            println(greeting)
            
        }
    }

    override fun close() {
        driver.close()
    }
}