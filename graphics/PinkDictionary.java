package android.graphics;

import android.os.AsyncTask;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class PinkDictionary {

    private static final String[] BAD_WORDS = { "kaist", "KAIST", "Wonsup", "Nak", "Bilgehan" };

    List<Node> nodes; // States in state transition
    int nodeCount = 0;
    Node n;
    int z;
    int[] arrayQueue;

    private static PinkDictionary instance = new PinkDictionary();

    public static PinkDictionary getInstance() {

        return instance;
    }

    private PinkDictionary() {
        this(BAD_WORDS);
        // Log.i("PinkDictionary", "new instance onStart()");
    }

    private PinkDictionary(String[] keys) {
        nodes = new ArrayList<Node>();
        int i, j, curnode;
        String s;
        char c;
        int a;

        n = new Node(-1, 0);
        nodes.add(n);
        nodeCount++;

        for (i = 0; i < keys.length; i++) // Construct the trie from words one by one
        {
            curnode = 0; // Start from root
            s = keys[i];
            // System.out.println(s);
            for (j = 0; j < s.length(); j++) {
                c = s.charAt(j);
                // Now, must check whether this leads to a transition. If not, create new node
                n = nodes.get(curnode);
                a = (n.children).indexOf(c);
                if (a != -1) // Go and trace the link
                    curnode = ((nodes.get(curnode)).transitions).get(a);
                else // Create a new node
                {
                    ((nodes.get(curnode)).children).add(c);
                    ((nodes.get(curnode)).transitions).add(nodeCount);
                    n = new Node(curnode, nodeCount);
                    nodes.add(n);
                    curnode = nodeCount;
                    nodeCount++;
                }
            }
            // The keyword has finished. Write the length of the bad word to help censoring
            nodes.get(curnode).output = s.length();
        }
        if (nodes == null)
            System.out.println("Nodes could not be constructed. Now at the beginning of fallback construction");
        curnode = 0;
        // An array implementation of a queue
        arrayQueue = new int[nodeCount];
        int queueIndex = 0; // To get array elements
        int usedNodes = 0; // To take elements out of queue
        int midState;
        char cc;

        arrayQueue[queueIndex++] = 0;
        for (i = 0; i < ((nodes.get(usedNodes)).transitions).size(); i++) {
            arrayQueue[queueIndex++] = ((nodes.get(usedNodes)).transitions).get(i);
        }
        usedNodes++; // Now the first part of fail function is done

        while (usedNodes < nodeCount) {
            for (i = 0; i < ((nodes.get(arrayQueue[usedNodes])).children).size(); i++) {
                cc = ((nodes.get(arrayQueue[usedNodes])).children).get(i); // The character we will check for
                                                                           // transitions

                arrayQueue[queueIndex++] = (nodes.get(arrayQueue[usedNodes])).transitions.get(i); // Put other
                                                                                                  // transitions to the
                                                                                                  // queue
                midState = (nodes.get(arrayQueue[usedNodes])).fallBack;
                while (midState != 0 && ((nodes.get(midState)).children).indexOf(cc) == -1) {
                    midState = (nodes.get(midState)).fallBack;
                }

                if ((nodes.get(midState)).children.indexOf(cc) != -1) {
                    nodes.get((nodes.get(arrayQueue[usedNodes]).transitions)
                            .get(i)).fallBack = ((nodes.get(midState)).transitions)
                                    .get((nodes.get(midState)).children.indexOf(cc));
                } else {
                    nodes.get((nodes.get(arrayQueue[usedNodes]).transitions).get(i)).fallBack = 0;
                }
            }
            usedNodes++; // Dequeue
        }

    }

    public char[] traverser(char[] s, int index, int count) {
        String s2 = new String(s, index, count);
        String s2_c = traverser(s2);
        for (int i = index; i < count; i++)
            s[i] = s2_c.charAt(i - index);
        return s;
    }

    public String traverser(String s) {
        // Log.i("PinkDictionary", s);
        int i, j;
        String processed = "";
        String depot = "";
        char c1;

        String punctuationList = "!?(.,;':`\"";
        if (this.nodes == null) {
            // System.out.println(z);
            return "Bad word list not given";
        }

        Node n = (this.nodes).get(0);

        boolean flag = true; // This will enable us not to censor compass, for example

        for (i = 0; i < s.length(); i++) {
            c1 = s.charAt(i);
            if (flag == false || (n.children).indexOf(c1) == -1) {
                if (depot != "") {
                    processed += depot;
                    depot = "";
                }
                processed += Character.toString(c1);
                if (flag == true)
                    n = nodes.get(n.fallBack); // Fall back to the safe place

                if (Character.isWhitespace(c1)) {
                    flag = true;
                    n = (this.nodes).get(0); // Start from scratch
                } else
                    flag = false; // Do not look for substrings. Like Dickens won't be censored
            } else {
                depot += Character.toString(c1);
                n = nodes.get((n.transitions).get((n.children).indexOf(c1)));
                if (n.output != 0) {
                    if (i == s.length() - 1 || Character.isWhitespace(s.charAt(i + 1))
                            || punctuationList.contains(Character.toString(s.charAt(i + 1)))) {
                        for (j = 0; j < n.output; j++) {
                            processed += "*";
                        }
                        depot = ""; // Clean the depot
                        n = (this.nodes).get(0);
                    } else {
                        flag = false;
                        processed += depot;
                        depot = "";
                    }
                }
            }
        }
        return processed;

    }

    private class Node {

        int identity;
        int parent;
        int fallBack;
        int output;

        List<Character> children;

        List<Integer> transitions;

        public Node(int par, int ide) {
            // TODO Auto-generated constructor stub

            // Define the identity, parent,children etc

            identity = ide;
            parent = par;
            children = new ArrayList<Character>(); // These are characters that lead to state transition
            transitions = new ArrayList<Integer>(); // Result of each state transition
            fallBack = 0; // node to go in the case of a failure
            output = 0; // Number of characters to put stars on, lenght of censored word
        }

    }
}
