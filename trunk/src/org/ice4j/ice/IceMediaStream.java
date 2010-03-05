/*
 * ice4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 * Maintained by the SIP Communicator community (http://sip-communicator.org).
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.ice4j.ice;

import java.util.*;
import java.util.logging.*;

import org.ice4j.*;

/**
 * The class represents a media stream from the ICE perspective, i.e. a
 * collection of components.
 *
 * @author Emil Ivov
 * @author Namal Senarathne
 */
public class IceMediaStream
{
    /**
     * Our class logger.
     */
    private static final Logger logger =
        Logger.getLogger(IceMediaStream.class.getName());

    /**
     * The name of this media stream. The name is equal to the value specified
     * in the SDP description.
     */
    private final String name;

    /**
     * Returns the list of components that this media stream consists of. A
     * component is a piece of a media stream requiring a single transport
     * address; a media stream may require multiple components, each of which
     * has to work for the media stream as a whole to work.
     */
    private final LinkedHashMap<Integer, Component> components
                                    = new LinkedHashMap<Integer, Component>();

    /**
     * The id that was last assigned to a component. The next id that we give
     * to a component would be lastComponendID + 1;
     */
    private int lastComponentID = 0;

    /**
     * The agent that this media stream belongs to.
     */
    private final Agent parentAgent;

    /**
     * Initializes a new <tt>IceMediaStream</tt> object.
     *
     * @param name the name of the media stream
     * @param parentAgent the agent that is handling the session that this
     * media stream is a part of
     */
    protected IceMediaStream(Agent parentAgent, String name)
    {
        this.name = name;
        this.parentAgent = parentAgent;
    }

    /**
     * Creates and adds a component to this media-stream
     * The component ID is incremented to the next integer value
     * when creating the component so make sure you keep that in mind in case
     * assigning a specific component ID is important to you.
     *
     * @param transport the transport protocol used by the component
     *
     * @return the newly created stream <tt>Component</tt> after adding it to
     * the stream first.
     */
    protected Component createComponent(Transport transport)
    {
        lastComponentID ++;

        Component component;
        synchronized( components )
        {
            component = new Component(lastComponentID, transport, this);
            components.put(new Integer(lastComponentID), component);
        }

        return component;
    }

    /**
     * Returns the name of this <tt>IceMediaStream</tt>.
     *
     * @return the name of this <tt>IceMediaStream</tt>.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns a <tt>String</tt> representation of this media stream.
     *
     * @return a <tt>String</tt> representation of this media stream.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer( "media stream:")
            .append(getName());
        buff.append(" (component count=").append(getComponentCount())
            .append(")");

        List<Component> components = getComponents();
        for (Component cmp : components)
        {
            buff.append("\n").append(cmp);
        }

        return buff.toString();
    }

    /**
     * Returns the <tt>Component</tt> with the specified <tt>id</tt> or
     * <tt>null</tt> if no such component exists in this stream.
     *
     * @param id the identifier of the component we are looking for.
     *
     * @return  the <tt>Component</tt> with the specified <tt>id</tt> or
     * <tt>null</tt> if no such component exists in this stream.
     */
    public Component getComponnet(int id)
    {
        synchronized(components)
        {
            return components.get(id);
        }
    }

    /**
     * Returns the list of <tt>Component</tt>s currently registered with this
     * stream.
     *
     * @return a non-null list of <tt>Component</tt>s currently registered with
     * this stream.
     */
    public List<Component> getComponents()
    {
        synchronized(components)
        {
            return new LinkedList<Component>(components.values());
        }
    }

    /**
     * Returns the number of <tt>Component</tt>s currently registered with this
     * stream.
     *
     * @return the number of <tt>Component</tt>s currently registered with this
     * stream.
     */
    public int getComponentCount()
    {
        synchronized(components)
        {
            return components.size();
        }
    }

    /**
     * Returns the IDs of all <tt>Component</tt>s currently registered with this
     * stream.
     *
     * @return a non-null list of IDs corresponding to the <tt>Component</tt>s
     * currently registered with this stream.
     */
    public List<Integer> getComponentIDs()
    {
        synchronized(components)
        {
            return new LinkedList<Integer>(components.keySet());
        }
    }

    /**
     * Returns the number of <tt>Component</tt>s currently registered with this
     * stream.
     *
     * @return the number of <tt>Component</tt>s currently registered with this
     * stream.
     */
    public int getStreamCount()
    {
        synchronized(components)
        {
            return components.size();
        }
    }

    /**
     * Returns a reference to the <tt>Agent</tt> that this stream belongs to.
     *
     * @return a reference to the <tt>Agent</tt> that this stream belongs to.
     */
    public Agent getParentAgent()
    {
        return parentAgent;
    }

    /**
     * Removes this stream and all <tt>Candidate</tt>s associated with its child
     * <tt>Component</tt>s.
     */
    protected void free()
    {
        synchronized (components)
        {
            Iterator<Map.Entry<Integer, Component>> cmpEntries
                            = components.entrySet().iterator();
            while (cmpEntries.hasNext())
            {
                Component component = cmpEntries.next().getValue();
                component.free();
                cmpEntries.remove();
            }
        }
    }

    /**
     * Removes <tt>component</tt> and all its <tt>Candidate</tt>s from the
     * this stream and releases all associated resources that they had
     * allocated (like sockets for example)
     *
     * @param component the <tt>Component</tt> we'd like to remove and free.
     */
    public void removeComponent(Component component)
    {
        synchronized (components)
        {
            components.remove(component.getComponentID());
            component.free();
        }
    }
}