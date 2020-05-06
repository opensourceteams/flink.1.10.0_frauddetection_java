/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport.v;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.walkthrough.common.entity.Alert;
import org.apache.flink.walkthrough.common.entity.Transaction;
import org.apache.flink.walkthrough.common.sink.AlertSink;
import org.apache.flink.walkthrough.common.source.TransactionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton code for the datastream walkthrough
 * 展示数据源,source
 */
public class FraudDetectionJob_v3 {
	private static final Logger LOG = LoggerFactory.getLogger(FraudDetectionJob_v3.class);

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		DataStream<Transaction> transactions = env
			.addSource(new TransactionSource())
			.name("transactions");

		transactions.keyBy(Transaction::getAccountId).addSink(new SinkFunction<Transaction>() {
			@Override
			public void invoke(Transaction value, Context context) throws Exception {
				LOG.info(value.toString());
			}
		});


		env.execute("Fraud Detection");
	}
}
