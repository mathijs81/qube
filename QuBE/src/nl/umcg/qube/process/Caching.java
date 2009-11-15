/* (C) 2009 Mathijs Vogelzang/University Medical Center Groningen.
 * For more information go to http://qube.sf.net/ 
 * 
 * This file is part of QuBE (Quantitative Blush Evaluator).
 * 
 * QuBE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * QuBE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with QuBE.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.umcg.qube.process;

import java.util.HashMap;

import nl.umcg.qube.model.CAGSequence;
import nl.umcg.qube.model.MoveCorrection;

/**
 * @author mathijs
 * 
 */
public class Caching {
    public static class CachingMoveCalculator implements IMoveCalculator {
        private IMoveCalculator calculator;
        private HashMap<String, MoveCorrection> cache = new HashMap<String, MoveCorrection>();

        public CachingMoveCalculator(IMoveCalculator moveCalculator) {
            calculator = moveCalculator;
        }

        @Override
        public MoveCorrection getMoveCorrection(CAGSequence sequence) {
            if (!cache.containsKey(sequence.getID().toString())) {
                cache.put(sequence.getID().toString(), calculator.getMoveCorrection(sequence));
            }
            return cache.get(sequence.getID().toString());
        }
    }
}
