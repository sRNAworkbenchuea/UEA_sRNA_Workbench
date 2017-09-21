package uk.ac.uea.cmp.srnaworkbench.tools.qualitycheck.distribution;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.hibernate.ScrollableResults;
import org.hibernate.type.Type;

/**
 * Implementation of ScrollableResults that is actually backed by a list. This 
 * simplifies methods used in DistributionService that were originally built around
 * ScrollableResults but I now have the data held in a list.
 * 
 * NOTE: not all methods are implemented - only the ones I needed. More can be implemented as needed.
 * 
 * @author Matthew Beckers
 */
public class ScrollableList implements ScrollableResults {
    
    private final Object[] list;
    private int p = -1;

    public ScrollableList(List toScroll) {
        this.list = toScroll.toArray();
    }

    public ScrollableList(Object[] toScroll) {
        this.list = toScroll;
    }

    private boolean hasResult() {
        return p < list.length && p >= 0;
    }

    @Override
    public boolean next() {
        p++;
        return hasResult();
    }

    @Override
    public boolean previous() {
        p--;
        return hasResult();
    }

    @Override
    public boolean scroll(int i) {
        p = i;
        return hasResult();
    }

    @Override
    public boolean last() {
        p = list.length-1;
        return list.length > 0;
    }

    @Override
    public boolean first() {
        p = 0;
        return list.length > 0;
    }

    @Override
    public void beforeFirst() {
        p = -1;
    }

    @Override
    public void afterLast() {
        p = list.length;
    }

    @Override
    public boolean isFirst() {
        return p == 0;
    }

    @Override
    public boolean isLast() {
        return p == list.length;
    }

    @Override
    public int getRowNumber() {
        return p;
    }

    @Override
    public boolean setRowNumber(int i) {
        if (i < 0) {
            p = list.length + i;
        } else {
            p = i;
        }
        return this.hasResult();
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public Object[] get() {
        return new Object[]{this.list[p]};
    }

    @Override
    public Object get(int i) {
        return this.list[p];
    }

    @Override
    public Type getType(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getInteger(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Long getLong(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Float getFloat(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean getBoolean(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getDouble(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Short getShort(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Byte getByte(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Character getCharacter(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getBinary(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getText(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Blob getBlob(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Clob getClob(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getString(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BigDecimal getBigDecimal(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BigInteger getBigInteger(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Date getDate(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Locale getLocale(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Calendar getCalendar(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimeZone getTimeZone(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
