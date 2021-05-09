package dk.cph.mmr;

import org.neo4j.driver.*;


import java.util.List;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

public class Neo4jConnection implements AutoCloseable {
    private final Driver driver;

    public Neo4jConnection(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }


    public void findNodeDomain( String nodeDomain )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    var result = tx.run( "match (nd:NodeDomain{name:$nodeDomain}) return nd;",
                            parameters( "nodeDomain", nodeDomain ) ).single().get("nd");
                    String i = result.get("name").toString();
                    String k = result.get("description").toString();
                    return i+" "+k;
                }
            } );
            System.out.println( greeting );
        }
    }
    public void countNodeDomainAttacedNodeTypes( String nodeDomain )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    var result = tx.run( "match (nd:NodeDomain{name:$nodeDomain})-[:INCLUDED_IN]-(nt) return count(nt) as count;",
                            parameters( "nodeDomain", nodeDomain ) ).single().get("count").asInt();

                    return result+"";
                }
            } );
            System.out.println( greeting );
        }
    }

    public void findNodeDomainsNodeTypes( String nodeDomain )
    {
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction(new TransactionWork<String>()
            {
                @Override
                public String execute(Transaction tx )
                {
                    final StringBuilder sb = new StringBuilder();
                    tx.run( "match (nd:NodeDomain{name:$nodeDomain})-[:INCLUDED_IN]-(nt) return nt;",
                            parameters( "nodeDomain", nodeDomain ) ).stream().map(x -> {
                        String i = x.get("nt").get("name").toString();
                        String k = x.get("nt").get("description").toString();
                        return i+" "+k+"\n";
                    }).forEach(k -> sb.append(k));
                    return sb.toString();
                }
            } );
            System.out.println( greeting );
        }
    }


    public static void main(String... args) throws Exception {
        try (Neo4jConnection neo = new Neo4jConnection("bolt://localhost:7687", "neo4j", "password")) {
            neo.countNodeDomainAttacedNodeTypes("Technical");
            //neo.findNodeDomainsNodeTypes("Technical");
        }
    }
}
