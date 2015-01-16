/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.ml.classifier.svm;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */

import java.io.Serializable;

/**
 * Stores a set of integer of a given size.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.5 $
 */
public class SMOSet implements Serializable {

    /**
     * for serialization
     */
    private static final long serialVersionUID = -8364829283188675777L;

    /**
     * The current number of elements in the set
     */
    private int m_number;

    /**
     * The first element in the set
     */
    private int m_first;

    /**
     * Indicators
     */
    private boolean[] m_indicators;

    /**
     * The next element for each element
     */
    private int[] m_next;

    /**
     * The previous element for each element
     */
    private int[] m_previous;

    /**
     * Creates a new set of the given size.
     */
    public SMOSet(int size) {

        m_indicators = new boolean[size];
        m_next = new int[size];
        m_previous = new int[size];
        m_number = 0;
        m_first = -1;
    }

    /**
     * Checks whether an element is in the set.
     */
    public boolean contains(int index) {

        return m_indicators[index];
    }

    /**
     * Deletes an element from the set.
     */
    public void delete(int index) {

        if (m_indicators[index]) {
            if (m_first == index) {
                m_first = m_next[index];
            } else {
                m_next[m_previous[index]] = m_next[index];
            }
            if (m_next[index] != -1) {
                m_previous[m_next[index]] = m_previous[index];
            }
            m_indicators[index] = false;
            m_number--;
        }
    }

    /**
     * Inserts an element into the set.
     */
    public void insert(int index) {

        if (!m_indicators[index]) {
            if (m_number == 0) {
                m_first = index;
                m_next[index] = -1;
                m_previous[index] = -1;
            } else {
                m_previous[m_first] = index;
                m_next[index] = m_first;
                m_previous[index] = -1;
                m_first = index;
            }
            m_indicators[index] = true;
            m_number++;
        }
    }

    /**
     * Gets the next element in the set. -1 gets the first one.
     */
    public int getNext(int index) {

        if (index == -1) {
            return m_first;
        } else {
            return m_next[index];
        }
    }

    /**
     * Prints all the current elements in the set.
     */
    public void printElements() {

        for (int i = getNext(-1); i != -1; i = getNext(i)) {
            System.err.print(i + " ");
        }
        System.err.println();
        for (int i = 0; i < m_indicators.length; i++) {
            if (m_indicators[i]) {
                System.err.print(i + " ");
            }
        }
        System.err.println();
        System.err.println(m_number);
    }

    /**
     * Returns the number of elements in the set.
     */
    public int numElements() {

        return m_number;
    }
}