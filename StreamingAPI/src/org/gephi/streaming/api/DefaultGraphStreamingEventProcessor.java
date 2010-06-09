/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.api;

import java.net.URL;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventListener;
import org.openide.util.Lookup;

/**
 * @author panisson
 *
 */
public class DefaultGraphStreamingEventProcessor implements GraphEventListener {
    
    private Graph graph;
    private OperationSupport graphUpdaterOperationSupport;
    
//    public DefaultGraphStreamingEventProcessor(Workspace workspace) {
//        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
//        if (workspace == null) {
//            workspace = pc.newWorkspace(pc.getCurrentProject());
//            pc.openWorkspace(workspace);
//        }
//        
//      //Architecture
//        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
//        GraphModel graphModel = graphController.getModel();
////        TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);
//
//        graph = graphModel.getHierarchicalMixedGraph();
//        graphUpdaterOperationSupport = new GraphUpdaterOperationSupport(graph);
//    }
    
    public DefaultGraphStreamingEventProcessor(Graph graph) {
        this.graph = graph;
        this.graphUpdaterOperationSupport = new GraphUpdaterOperationSupport(graph);
    }
    
    /**
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }
    
    public void process(URL url, String streamType) {
        
        
        GraphEventContainerFactory containerfactory = Lookup.getDefault().lookup(GraphEventContainerFactory.class);
        GraphEventContainer container = containerfactory.newGraphEventContainer(url);
        container.getGraphEventDispatcher().addEventListener(this);
        GraphEventOperationSupport eventOperationSupport = new GraphEventOperationSupport(container);
        
        StreamReaderFactory processorFactory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = processorFactory.createStreamReader(streamType, eventOperationSupport);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, processor);
    }
    
    public void process(GraphStreamingEndpoint endpoint) {
        this.process(endpoint.getUrl(), endpoint.getStreamType().getType());
    }
    
    @Override
    public void onGraphEvent(GraphEvent event) {
        
        if (event instanceof ElementEvent) {
            if (event instanceof ElementAttributeEvent) {

                ElementAttributeEvent attributeEvent = (ElementAttributeEvent)event;

                if(event.getElementType() == ElementType.NODE) {

                    switch (event.getEventType()) {

                    case ADD:
                        graphUpdaterOperationSupport.nodeAttributeAdded(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName(), 
                                attributeEvent.getAttributeValue());
                        break;
                        
                    case CHANGE: 
                        
                        graphUpdaterOperationSupport.nodeAttributeChanged(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName(), 
                                attributeEvent.getAttributeValue());
                        break;

                    case REMOVE:
                        graphUpdaterOperationSupport.nodeAttributeRemoved(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName());
                        break;
                    }
                }
                
                else if(event.getElementType() == ElementType.EDGE) {

                    switch (event.getEventType()) {

                    case ADD:
                        graphUpdaterOperationSupport.edgeAttributeAdded(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName(), 
                                attributeEvent.getAttributeValue());
                        break;
                        
                    case CHANGE: 
                        
                        graphUpdaterOperationSupport.edgeAttributeChanged(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName(), 
                                attributeEvent.getAttributeValue());
                        break;

                    case REMOVE:
                        graphUpdaterOperationSupport.edgeAttributeRemoved(
                                attributeEvent.getElementId(), attributeEvent.getAttributeName());
                        break;
                    }

                }
            }
            
            else {
                    
               ElementEvent elementEvent = (ElementEvent)event;

               if(event.getElementType() == ElementType.NODE) {

                   switch (event.getEventType()) {

                   case ADD:
                       graphUpdaterOperationSupport.nodeAdded(elementEvent.getElementId());
                       break;

                   case CHANGE:
                       System.out.println("Invalid change operation on node "+elementEvent.getElementId());
                       break;

                   case REMOVE:
                       graphUpdaterOperationSupport.nodeRemoved(elementEvent.getElementId());
                       break;
                   }
               }
               else if(event.getElementType() == ElementType.EDGE) {
                   
                   switch (event.getEventType()) {

                   case ADD:
                       EdgeAddedEvent edgeAddedEvent = (EdgeAddedEvent) event;
                       graphUpdaterOperationSupport.edgeAdded(elementEvent.getElementId(), 
                               edgeAddedEvent.getSourceId(), edgeAddedEvent.getTargetId(), edgeAddedEvent.isDirected());
                       break;

                   case CHANGE:
                       System.out.println("Invalid change operation on edge "+elementEvent.getElementId());
                       break;

                   case REMOVE:
                       graphUpdaterOperationSupport.edgeRemoved(elementEvent.getElementId());
                       break;
                   }
               }
            }
        }
    }

}