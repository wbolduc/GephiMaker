/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gephimaker;

/**
 *
 * @author wbolduc
 */
public class EdgeData {
    final public int count;
    final public double sentiment;

    EdgeData(int count, double sentiment)
    {
        this.count = count;
        this.sentiment = sentiment;
    }
}