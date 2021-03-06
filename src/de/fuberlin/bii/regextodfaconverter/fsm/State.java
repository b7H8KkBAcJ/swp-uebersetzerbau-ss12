/*
 * 
 * Copyright 2012 lexergen.
 * This file is part of lexergen.
 * 
 * lexergen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * lexergen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with lexergen.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * lexergen:
 * A tool to chunk source code into tokens for further processing in a compiler chain.
 * 
 * Projectgroup: bi, bii
 * 
 * Authors: Daniel Rotar
 * 
 * Module:  Softwareprojekt Übersetzerbau 2012 
 * 
 * Created: Apr. 2012 
 * Version: 1.0
 *
 */

package de.fuberlin.bii.regextodfaconverter.fsm;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * Stellt einen Zustand eines endlicher Automaten (bzw. einer Zustandsmaschine)
 * dar.
 * 
 * @author Daniel Rotar
 * 
 * @param <TransitionConditionType>
 *            Der Typ der Bedingung für einen Zustandsübergang.
 * @param <PayloadType>
 *            Der Typ des Inhalts.
 */
public class State<TransitionConditionType extends Serializable, PayloadType extends Serializable>
		implements de.fuberlin.bii.tokenmatcher.State<TransitionConditionType, PayloadType> {

	/**
	 * UID für die Serialisierung/Abspeicherung als *.dfa
	 */
	private static final long serialVersionUID = 9083069509544119290L;

	/**
	 * Die eindetige UUID dieses Zustandes.
	 */
	private UUID _uuid;
	/**
	 * Der in diesem Zustand hinterlegte Inhalt.
	 */
	private PayloadType _payload;
	/**
	 * Die Übergänge, die von diesem Zustand möglich sind.
	 */
	private HashSet<Transition<TransitionConditionType, PayloadType>> _transitions;
	/**
	 * Die Flag für den Startzusand.
	 */
	private boolean _initial = false;
	/**
	 * Die Flag für den Endzustand.
	 */
	private boolean _finite = false;

	/**
	 * Gibt die eindetige UUID dieses Zustandes zurück.
	 * 
	 * @return Die eindetige UUID dieses Zustandes.
	 */
	public UUID getUUID() {
		return _uuid;
	}

	/**
	 * Gibt den in diesem Zustand hinterlegte Inhalt zurück. Setzt die eindetige
	 * UUID dieses Zustandes fest.
	 * 
	 * @param uuid
	 *            Die eindetige UUID dieses Zustandes.
	 */
	private void setUUID(UUID uuid) {
		_uuid = uuid;
	}

	/**
	 * Generiert eine neue UUID für diesen Zustand.
	 */
	private void generateNewUUID() {
		setUUID(UUID.randomUUID());
	}

	/**
	 * Gibt den in diesem Zustand hinterlegte Inhalt zurück.
	 * 
	 * @return Der in diesem Zustand hinterlegte Inhalt.
	 */
	public PayloadType getPayload() {
		return _payload;
	}

	/**
	 * Setzt den in diesem Zustand zu hinterlegenden Inhalt fest.
	 * 
	 * @param payload
	 *            Der in diesem Zustand zu hinterlegende Inhalt.
	 */
	public void setPayload(PayloadType payload) {
		_payload = payload;
	}

	/**
	 * Gibt die Übergänge, die von diesem Zustand möglich sind zurück.
	 * 
	 * @return Die Übergänge, die von diesem Zustand möglich sind.
	 */
	public HashSet<Transition<TransitionConditionType, PayloadType>> getTransitions() {
		return _transitions;
	}

	/**
	 * Setzt die Übergänge, die von diesem Zustand möglich sind fest.
	 * 
	 * @param transitions
	 *            Die Übergänge, die von diesem Zustand möglich sind.
	 */
	private void setTransitiosn(
			HashSet<Transition<TransitionConditionType, PayloadType>> transitions) {
		_transitions = transitions;
	}

	/**
	 * Gibt alle Elemente, die den ausgehenden Übergängen zugeordnet sind
	 * zurück.
	 * 
	 * @return Alle Elemente, die den ausgehenden Übergängen zugeordnet sind
	 *         zurück.
	 */
	public Collection<TransitionConditionType> getElementsOfOutgoingTransitions() {
		HashSet<TransitionConditionType> elements = new HashSet<TransitionConditionType>();

		for (Transition<TransitionConditionType, PayloadType> tran : getTransitions()) {
			elements.add(tran.getCondition());
		}

		return elements;
	}

	/**
	 * Gibt die Flag für den Startzusand zurück.
	 * 
	 * @return Die Flag für den Startzusand.
	 */
	protected boolean getInitial() {
		return _initial;
	}

	/**
	 * Setzt die Flag für den Startzusand fest.
	 * 
	 * @param initial
	 *            Die Flag für den Startzusand.
	 */
	protected void setInitial(boolean initial) {
		_initial = initial;
	}

	/**
	 * Gibt die Flag für den Endzustand zurück.
	 * 
	 * @return Die Flag für den Endzustand.
	 */
	public boolean getFinite() {
		return _finite;
	}

	/**
	 * Setzt die Flag für den Endzustand fest.
	 * 
	 * @param finite
	 *            Die Flag für den Endzustand.
	 */
	public void setFinite(boolean finite) {
		_finite = finite;
	}

	/**
	 * Setzt den Zustandtypen dieses Zustands auf einen exklusiven Startzustand.
	 */
	protected void setTypeToInitial() {
		setInitial(true);
		setFinite(false);
	}

	/**
	 * Setzt den Zustandtypen dieses Zustands auf einen exklusiven Endzustand.
	 */
	public void setTypeToFinite() {
		setInitial(false);
		setFinite(true);
	}

	/**
	 * Setzt den Zustandtypen dieses Zustands auf einen exklusiven
	 * Default-Zustand.
	 */
	public void setTypeToDefault() {
		setInitial(false);
		setFinite(false);
	}

	/**
	 * Legt den Zustandstyp auf INITIAL und FINITE fest.
	 */
	protected void setTypeToInitialAndFinite() {
		setInitial(true);
		setFinite(true);
	}

	/**
	 * Gibt an, ob es sich bei diesem Zustand um einen Endzustand handelt.
	 * 
	 * @return true, wenn es sich um einen Endzustand handelt, sonst false.
	 */
	public boolean isFiniteState() {
		return getFinite();
	}

	/**
	 * Gibt an, ob es sich bei diesem Zustand um einen Anfangszustand handelt.
	 * 
	 * @return true, wenn es sich um einen Anfangszustand handelt, sonst false.
	 */
	public boolean isInitialState() {
		return getInitial();
	}

	/**
	 * Gibt an, ob es sich bei diesem Zustand um einen Default-Zustand handelt.
	 * 
	 * @return true, wenn es sich um keinen Anfangszustand und keinen Endzustand
	 *         handelt, sonst false.
	 */
	public boolean isDefaultState() {
		return (!(getInitial() && getFinite()));
	}

	/**
	 * Fügt dem aktuellen Zustand einen Nachfolgezustand hinzu.
	 * 
	 * @param condition
	 *            Die Bedingung für den Zustandsübergang.
	 * @param state
	 *            Der einzufügende Nachfolgezustand.
	 * @return true, wenn der Zustand mit der angegebenen Bedingung noch nicht noch nicht vorhanden war, sonst false.
	 */
	protected boolean addState(TransitionConditionType condition,
			State<TransitionConditionType, PayloadType> state) {
		return getTransitions().add(new Transition<TransitionConditionType, PayloadType>(condition, state));
	}

	@Override
	public boolean equals(Object o) {
	    if ( this == o) 
    	{
	    	return true;
    	}
	    
	    if (o == null)
	    {
	    	return false;
	    }
	    
	    if (!(o instanceof State<?,?>))
    	{
	    	return false;
    	}
	    else
	    {
	    	State<?,?> s = (State<?,?>)o;
	    	if (s.getUUID().equals(getUUID())) {
	    		return true;
	    	}
	    	else
	    	{
	    		return false;
	    	}
	    }
	}

	/**
	 * Erstellt ein neues State Objekt.
	 */
	public State() {
		generateNewUUID();
		setPayload(null);
		setTransitiosn(new HashSet<Transition<TransitionConditionType, PayloadType>>());
		setTypeToDefault();
	}

	/**
	 * Erstellt ein neues State Objekt.
	 * 
	 * @param payload
	 *            Der in diesem Zustand hinterlegte Inhalt.
	 */
	public State(PayloadType payload) {
		this();
		setPayload(payload);
	}

	/**
	 * Erstellt ein neues State Objekt.
	 * 
	 * @param isFinite
	 *            Gibt an, ob es sich bei diesem Zustand um einen Endzustand
	 *            handelt.
	 */
	public State(boolean isFinite) {
		this();
		if (isFinite)
			setTypeToFinite();
	}

	/**
	 * Erstellt ein neues State Objekt.
	 * 
	 * @param payload
	 *            Der in diesem Zustand hinterlegte Inhalt.
	 * @param isFinite
	 *            Gibt an, ob es sich bei diesem Zustand um einen Endzustand
	 *            handelt.
	 */
	public State(PayloadType payload, boolean isFinite) {
		this();
		setPayload(payload);
		if (isFinite)
			setTypeToFinite();
	}

}
