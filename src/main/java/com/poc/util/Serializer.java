package com.poc.util;

import java.io.StringWriter;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.io.IOException;
import java.io.Writer;
import java.io.FilterWriter;

public class Serializer extends FilterWriter
{
    private static boolean[] unsafeChars;
    private static final int SMALL_STRING_LIMIT = 40;
    private static final int BUF_LEN = 128;
    private final char[] buf;
    private int bufIndex;
    
    public Serializer(final Writer writer) {
        super(writer);
        this.buf = new char[128];
        this.bufIndex = 0;
    }
    
    private void flushBuffer() throws IOException {
        if (bufIndex > 0) {
            write(this.buf, 0, this.bufIndex);
            bufIndex = 0;
        }
    }
    
    final void writeOutput(final int character) throws IOException {
        if (bufIndex >= 128) {
            flushBuffer();
        }
        this.buf[this.bufIndex++] = (char)character;
    }
    
    private final void serializeMap(final Map<?, ?> h) throws IOException {
        final Iterator<?> it = h.keySet().iterator();
        writeOutput(123);
        while (it.hasNext()) {
            final Object key = it.next();
            final Object value = h.get(key);
            if (key instanceof String) {
                serializeString((String)key);
            } else {
                serializeObjectInternal(key);
            }
            writeOutput(61);
            if (value instanceof String) {
                serializeString((String)value);
            } else {
                serializeObjectInternal(value);
            }
            writeOutput(59);
        }
        writeOutput(125);
    }
    
    private final void serializeArray(final Object[] a) throws IOException {
        writeOutput(91);
        for (int i = 0, c = a.length; i < c; ++i) {
            final Object o = a[i];
            if (o instanceof String) {
                serializeString((String)o);
            } else {
                serializeObjectInternal(o);
            }
            if (i < c - 1) {
                writeOutput(44);
            }
        }
        writeOutput(93);
    }
    
    private final void serializeList(final List v) throws IOException {
        writeOutput(40);
        for (int i = 0, c = v.size(); i < c; ++i) {
            final Object o = v.get(i);
            if (o instanceof String) {
                serializeString((String)o);
            } else {
                serializeObjectInternal(o);
            }
            if (i < c - 1) {
                writeOutput(44);
            }
        }
        writeOutput(41);
    }
    
    final boolean stringRequiresQuotes(final String s) {
        for (int i = 0, c = s.length(); i < c; ++i) {
            final char ch = s.charAt(i);
            if (ch >= '\u007f') {
                return true;
            }
            if (Serializer.unsafeChars[ch]) {
                return true;
            }
        }
        return false;
    }
    
    private final boolean stringRequiresQuotes(final char[] str) {
        for (int i = 0, c = str.length; i < c; ++i) {
            final char ch = str[i];
            if (ch >= '\u007f') {
                return true;
            }
            if (Serializer.unsafeChars[ch]) {
                return true;
            }
        }
        return false;
    }
    
    private final int fourBitToAscii(final int n) {
        if (n < 10) {
            return 48 + n;
        }
        return 65 + (n - 10);
    }
    
    private void serializeCharacters(final char[] str, final String s, final int length, final boolean shouldUseArray) throws IOException {
        for (int i = 0; i < length; ++i) {
            char ch;
            if (shouldUseArray) {
                ch = str[i];
            } else {
                ch = s.charAt(i);
            }
            if (ch < '\u00ff') {
                if (ch >= '#' && ch <= '~' && ch != '\\') {
                    writeOutput(ch);
                }
                else {
                    switch (ch) {
                        case ' ':
                        case '!': {
                            writeOutput(ch);
                            break;
                        }
                        case '\"': {
                            writeOutput(92);
                            writeOutput(34);
                            break;
                        }
                        case '\t': {
                            writeOutput(92);
                            writeOutput(116);
                            break;
                        }
                        case '\n': {
                            writeOutput(92);
                            writeOutput(110);
                            break;
                        }
                        case '\r': {
                            writeOutput(92);
                            writeOutput(114);
                            break;
                        }
                        case '\\': {
                            writeOutput(92);
                            writeOutput(92);
                            break;
                        }
                        default: {
                            writeOutput(ch);
                            break;
                        }
                    }
                }
            } else {
                writeOutput(ch);
            }
        }
    }
    
    void serializeString(final String s) throws IOException {
        if (s == null) {
            writeOutput(34);
            writeOutput(34);
            return;
        }
        final int length = s.length();
        if (length == 0) {
            writeOutput(34);
            writeOutput(34);
            return;
        }
        final boolean shouldUseArray = length > 8;
        char[] str;
        if (shouldUseArray) {
            str = s.toCharArray();
        }
        else {
            str = null;
        }
        boolean shouldUseQuote;
        if (length > 40) {
            shouldUseQuote = true;
        }
        else if (shouldUseArray) {
            shouldUseQuote = stringRequiresQuotes(str);
        }
        else {
            shouldUseQuote = stringRequiresQuotes(s);
        }
        if (shouldUseQuote) {
            writeOutput(34);
        }
        this.serializeCharacters(str, s, length, shouldUseArray);
        if (shouldUseArray) {
            str = null;
        }
        if (shouldUseQuote) {
            writeOutput(34);
        }
    }
    
    final void serializeNull() throws IOException {
        writeOutput(64);
    }
    
    private final void serializeObjectInternal(final Object anObject) throws IOException {
        if (anObject instanceof String) {
            serializeString((String)anObject);
        } else if (anObject instanceof Map) {
            serializeMap((Map)anObject);
        } else if (anObject instanceof Object[]) {
            serializeArray((Object[])anObject);
        } else if (anObject instanceof List) {
            serializeList((List)anObject);
        } else if (anObject == null) {
            serializeNull();
        } else {
            serializeString(anObject.toString());
        }
    }
    
    @Override
    public void flush() throws IOException {
        this.flushBuffer();
        super.flush();
    }
    
    public void writeObject(final Object anObject) throws IOException {
        serializeObjectInternal(anObject);
    }
    
    public static String serializeObject(final Object anObject) {
        String result = null;
        if (anObject == null) {
            result = null;
        } else {
            StringWriter memory = new StringWriter();
            Serializer serializer = new Serializer((Writer)memory);
            try {
                serializer.writeObject(anObject);
                serializer.flush();
            } catch (IOException ex) {

            }
            result = memory.toString();
            
            try {
                serializer.close();
                memory.close();
            } catch (IOException ex2) {

            }
            
            memory = null;
            serializer = null;
        }
        return result;
    }
    
    public static boolean writeObject(final Writer writer, final Object anObject) {
        try {
            final Serializer serializer = new Serializer(writer);
            serializer.writeObject(anObject);
            serializer.flush();
            serializer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    static {
        Serializer.unsafeChars = new boolean[127];
        for (int i = 0; i < 32; ++i) {
            Serializer.unsafeChars[i] = true;
        }
        Serializer.unsafeChars[32] = true;
        Serializer.unsafeChars[34] = true;
        Serializer.unsafeChars[91] = true;
        Serializer.unsafeChars[93] = true;
        Serializer.unsafeChars[44] = true;
        Serializer.unsafeChars[40] = true;
        Serializer.unsafeChars[41] = true;
        Serializer.unsafeChars[123] = true;
        Serializer.unsafeChars[125] = true;
        Serializer.unsafeChars[61] = true;
        Serializer.unsafeChars[59] = true;
        Serializer.unsafeChars[47] = true;
        Serializer.unsafeChars[64] = true;
        Serializer.unsafeChars[33] = true;
        Serializer.unsafeChars[35] = true;
        Serializer.unsafeChars[36] = true;
        Serializer.unsafeChars[37] = true;
        Serializer.unsafeChars[38] = true;
        Serializer.unsafeChars[39] = true;
        Serializer.unsafeChars[58] = true;
        Serializer.unsafeChars[60] = true;
        Serializer.unsafeChars[62] = true;
        Serializer.unsafeChars[63] = true;
        Serializer.unsafeChars[92] = true;
        Serializer.unsafeChars[94] = true;
        Serializer.unsafeChars[96] = true;
        Serializer.unsafeChars[124] = true;
        Serializer.unsafeChars[126] = true;
    }
}