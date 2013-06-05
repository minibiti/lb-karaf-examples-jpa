/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.lburgazzoli.osgi.hazelcast;

import com.github.lburgazzoli.Utils;
import com.github.lburgazzoli.osgi.BundleContextAware;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.InstanceEvent;
import com.hazelcast.core.InstanceListener;
import com.hazelcast.core.MultiMap;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class HazelcastManager extends BundleContextAware implements IHazelcastManager, InstanceListener {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(HazelcastManager.class);

    private HazelcastInstance m_instance;
    private Config m_config;
    private AtomicBoolean m_active;
    private ClassLoader m_classLoader;

    /**
     * c-tor
     *
     * @param bundleContext
     * @param config
     */
    public HazelcastManager(BundleContext bundleContext,Config config) {
        this(bundleContext,config,Thread.currentThread().getContextClassLoader());
    }

    /**
     * c-tor
     *
     * @param bundleContext
     * @param config
     * @param classLoader
     */
    public HazelcastManager(BundleContext bundleContext,Config config,ClassLoader classLoader) {
        super(bundleContext);

        m_active = new AtomicBoolean(false);
        m_config = config;
        m_classLoader = classLoader;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public void init() {
        if(m_instance == null) {
            LOGGER.debug("Initialize Instance");

            m_instance = Hazelcast.newHazelcastInstance(m_config);
            m_instance.addInstanceListener(this);

            LOGGER.debug("New Instance created     : {}", m_instance);
            LOGGER.debug("New Instance ClassLoader : {}", m_instance.getConfig().getClassLoader());

            m_active.set(true);
        }
    }

    public void destroy() {
        if(m_instance != null) {
            m_active.set(false);

            LOGGER.debug("Destroy instance {}", m_instance);

            m_instance.removeInstanceListener(this);
            m_instance.getLifecycleService().shutdown();
            m_instance = null;
        }
    }

    // *************************************************************************
    // IHazelcastManager
    // *************************************************************************

    /**
     *
     * @return
     */
    public String getId() {
        return m_instance.getCluster().getLocalMember().getUuid();
    }

    @Override
    public HazelcastInstance getInstance() {
        return m_instance;
    }

    @Override
    public ClassLoader getClassloader() {
        return m_instance.getConfig().getClassLoader();
    }

    @Override
    public <K,V> IMap<K,V> getMap(String mapName) {
        ClassLoader cl = Utils.swapContextClassLoader(getClassloader());
        IMap<K,V> rv = null;

        try {
            rv = m_instance.getMap(mapName);
        } finally {
            Utils.swapContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public <K,V> MultiMap<K,V> getMultiMap(String mapName) {
        ClassLoader cl = Utils.swapContextClassLoader(getClassloader());
        MultiMap<K,V> rv = null;

        try {
            rv = m_instance.getMultiMap(mapName);
        } finally {
            Utils.swapContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public <T> IList<T> getList(String listName) {
        ClassLoader cl = Utils.swapContextClassLoader(getClassloader());
        IList<T> rv = null;

        try {
            rv = m_instance.getList(listName);
        } finally {
            Utils.swapContextClassLoader(cl);
        }

        return  rv;
    }

    public <T> IQueue<T> getQueue(String queueName) {
        ClassLoader cl = Utils.swapContextClassLoader(getClassloader());
        IQueue<T> rv = null;

        try {
            rv = m_instance.getQueue(queueName);
        } finally {
            Utils.swapContextClassLoader(cl);
        }

        return  rv;
    }

    @Override
    public ILock getLock(String lockName) {
        return m_instance.getLock(lockName);
    }

    @Override
    public <T> ITopic<T> getTopic(String topicName) {
        ClassLoader cl = Utils.swapContextClassLoader(getClassloader());
        ITopic<T> rv = null;

        try {
            rv = m_instance.getTopic(topicName);
        } finally {
            Utils.swapContextClassLoader(cl);
        }

        return  rv;
    }

    // *************************************************************************
    // InstanceListener
    // *************************************************************************

    /**
     *
     */
    @Override
    public void instanceCreated(InstanceEvent event) {
        LOGGER.debug("Hazelcast Instance Created : {}",event.getInstance().getId());
    }

    /**
     *
     */
    @Override
    public void instanceDestroyed(InstanceEvent event) {
        LOGGER.debug("Hazelcast Instance Destroyed : {}",event.getInstance().getId());
    }
}
