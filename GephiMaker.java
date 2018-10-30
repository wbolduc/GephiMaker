/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gephimaker;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import multicorenlp.BWord;
import multicorenlp.SVO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import structuralbalancer.Node;
import structuralbalancer.Connection;
import structuralbalancer.SequentialUID;

/**
 *
 * @author wbolduc
 */
public class GephiMaker {

    public static void main(String[] args) throws IOException {
        //reading arguments
        if(args.length == 0)
        {
            System.out.println("No input file given");
            System.exit(0);
        }
        
        if(args[0].equals("-h"))
        {
            System.out.println("This tool generates to two files to be used in gephi");
            System.exit(0);
        }
        
        String inFile = args[0];
        inFile = FilenameUtils.normalize(inFile);
        if(inFile == null)
        {
            System.out.println("Not a valid file path");
            System.exit(0);
        }
        if(FilenameUtils.getExtension(inFile).equals("csv") != true)
        {
            System.out.println("Input file must be csv");
            System.exit(0);
        }
        
        
        HashMap<BWord, Node> graph = new HashMap<>();
        ArrayList<Pair<EdgeData, SVO>> svoCounts = new ArrayList();//sampleGraph1();

        System.out.println("Loading " + inFile + "...");
        svoCounts = loadAllCSVTweets(inFile);

        System.out.println("Building graph...");
        svoCounts.forEach(svoIntPair ->
        {
            EdgeData conData = svoIntPair.getKey();

            SVO svo = svoIntPair.getValue();

            BWord subject = svo.getSubject();   //get words
            BWord object  = svo.getObject();

            Node sub = graph.get(subject);      //get associated nodes
            Node obj = graph.get(object);

            if(sub == null)
            {
                sub = new Node(subject);
                graph.put(subject, sub);
            }
            if(obj == null)
            {
                obj = new Node(object);
                graph.put(object, obj);
            }

            Connection connection = new Connection(svo.getVerb(), conData.count, conData.sentiment);

            sub.connect(obj, connection);
        });

        System.out.println("Making Gephi Files...");
        outputGephiFiles(graph, inFile);
        System.out.println("Done");
    }

    private static ArrayList<Pair<EdgeData, SVO>> sampleGraph1()
    {
        ArrayList<Pair<EdgeData, SVO>> sample = new ArrayList<>();

        //type1 -  1 transitive triple
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("1A", false), new BWord("verb", false), new BWord("1B", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("1A", false), new BWord("verb", false), new BWord("1C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("1C", false), new BWord("verb", false), new BWord("1B", false), 0)));

        //type2 - 2 transitive triples
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("2A", false), new BWord("verb", false), new BWord("2C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("2C", false), new BWord("verb", false), new BWord("2A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("2B", false), new BWord("verb", false), new BWord("2A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("2B", false), new BWord("verb", false), new BWord("2C", false), 0)));

        //type3 - 2 transitive triples
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("3A", false), new BWord("verb", false), new BWord("3C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("3C", false), new BWord("verb", false), new BWord("3A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("3A", false), new BWord("verb", false), new BWord("3B", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("3C", false), new BWord("verb", false), new BWord("3B", false), 0)));

        //type4 - 1 transitive triple 1 cyclic triple
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("4A", false), new BWord("verb", false), new BWord("4C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("4C", false), new BWord("verb", false), new BWord("4A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("4A", false), new BWord("verb", false), new BWord("4B", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("4B", false), new BWord("verb", false), new BWord("4C", false), 0)));

        //type5 - 3 transitive triples 1 cyclic triple
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("5A", false), new BWord("verb", false), new BWord("5C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("5C", false), new BWord("verb", false), new BWord("5A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("5B", false), new BWord("verb", false), new BWord("5A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("5B", false), new BWord("verb", false), new BWord("5C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("5C", false), new BWord("verb", false), new BWord("5B", false), 0)));

        //type6 - 6 transitive triples 2 cyclic triple
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6A", false), new BWord("verb", false), new BWord("6B", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6A", false), new BWord("verb", false), new BWord("6C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6B", false), new BWord("verb", false), new BWord("6A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6B", false), new BWord("verb", false), new BWord("6C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6C", false), new BWord("verb", false), new BWord("6A", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("6C", false), new BWord("verb", false), new BWord("6B", false), 0)));

        //type7 - 1 cyclic triple
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("7A", false), new BWord("verb", false), new BWord("7B", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("7B", false), new BWord("verb", false), new BWord("7C", false), 0)));
        sample.add(new Pair(new EdgeData(10,1.2), new SVO(new BWord("7C", false), new BWord("verb", false), new BWord("7A", false), 0)));

        return sample;
    }


    public static void outputGephiFiles(HashMap<BWord, Node> graph, String outFile) throws IOException
    {
        String fileNoExtension = FilenameUtils.getFullPath(outFile) + FilenameUtils.getBaseName(outFile);
        
        SequentialUID ids;
        ids = new SequentialUID();

        //assign IDs
        graph.values().forEach(key -> ids.uid(key));

        CSVPrinter NODES = new CSVPrinter(new BufferedWriter(new FileWriter(fileNoExtension + "_GephiNodes.csv")), CSVFormat.DEFAULT.withHeader("id", "label", "negated"));
        CSVPrinter EDGES = new CSVPrinter(new BufferedWriter(new FileWriter(fileNoExtension + "_GephiEdges.csv")), CSVFormat.DEFAULT.withHeader("Source", "Target", "Verb", "Negated", "Sentiment", "Count"));


        graph.values().forEach(node -> {
            try {
                //store Node

                NODES.printRecord(ids.uid(node),node.subject.word, node.subject.negated); //store node
            } catch (IOException ex) {
                Logger.getLogger(GephiMaker.class.getName()).log(Level.SEVERE, null, ex);
            }


            //store Edge
            node.getConnections().entrySet().forEach(connection -> {
                Node dest = connection.getKey();
                ArrayList<Connection> edge = connection.getValue();

                if (edge.size() > 0)
                    edge.forEach(link -> {
                    try {
                        EDGES.printRecord(ids.uid(node), ids.uid(dest), link.verb.word, link.verb.negated, link.sentiment, link.connectionCount);
                    } catch (IOException ex) {
                        Logger.getLogger(GephiMaker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    });
            });
        });

        NODES.close();
        EDGES.close();
    }

    public static ArrayList<Pair<EdgeData, SVO>> loadAllCSVTweets(String fileName) throws FileNotFoundException, IOException
    {
        ArrayList<Pair<EdgeData, SVO>> svos = new ArrayList<>();

        Reader csvData = new FileReader(fileName);
        CSVFormat format =  CSVFormat.DEFAULT.withFirstRecordAsHeader();
        Iterable<CSVRecord> records = format.parse(csvData);


        records.forEach(rec -> svos.add(new Pair(   new EdgeData( Integer.parseInt(rec.get("count")),
                                                                        Double.parseDouble(rec.get("sentiment"))),
                                                    new SVO(rec.get("subject"),
                                                            Boolean.parseBoolean(rec.get("subjectNegated")),
                                                            rec.get("verb"),
                                                            Boolean.parseBoolean(rec.get("verbNegated")),
                                                            rec.get("object"),
                                                            Boolean.parseBoolean(rec.get("objectNegated")),0))));

        csvData.close();
        return svos;
    }
}
