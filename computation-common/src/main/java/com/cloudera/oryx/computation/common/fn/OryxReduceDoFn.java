/*
 * Copyright (c) 2013, Cloudera, Inc. All Rights Reserved.
 *
 * Cloudera, Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"). You may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.cloudera.oryx.computation.common.fn;

import com.cloudera.oryx.common.servcomp.OryxConfiguration;
import com.cloudera.oryx.common.settings.ConfigUtils;
import com.cloudera.oryx.computation.common.JobStep;

import com.google.common.base.Preconditions;
import org.apache.crunch.DoFn;
import org.apache.crunch.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OryxReduceDoFn<K, V, T> extends DoFn<Pair<K, V>, T> {

  private static final Logger log = LoggerFactory.getLogger(OryxReduceDoFn.class);

  private OryxConfiguration configuration;
  private int partition;
  private int numPartitions;

  @Override
  protected final OryxConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public void initialize() {
    super.initialize();
    this.configuration = new OryxConfiguration(getContext().getConfiguration());

    ConfigUtils.overlayConfigOnDefault(configuration.get(JobStep.CONFIG_SERIALIZATION_KEY));

    numPartitions = getContext().getNumReduceTasks();
    partition = configuration.getInt("mapred.task.partition", -1);
    log.info("Partition index {} ({} total)", partition, numPartitions);
    Preconditions.checkArgument(numPartitions > 0, "# partitions must be positive: %s", numPartitions);
    Preconditions.checkArgument(partition >= 0 && partition < numPartitions,
        "Partitions must be in [0,# partitions): %s",
        partition);
  }

  /**
   * @return reducer number, out of {@link #getNumPartitions()}
   */
  protected final int getPartition() {
    return partition;
  }

  /**
   * @return total number of reducers
   */
  protected final int getNumPartitions() {
    return numPartitions;
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName();
  }
}
