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

package spendreport.v.bounded;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.walkthrough.common.entity.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skeleton code for the datastream walkthrough
 * 展示数据源,source
 */
public class FraudDetectionJob_v1 {
	private static final Logger LOG = LoggerFactory.getLogger(FraudDetectionJob_v1.class);

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		//env.setParallelism(5);

		DataStream<Transaction> transactions = env
			.addSource(new TransactionSourceBounded())
			.name("transactions");


		SingleOutputStreamOperator<Object> dataStream2 = transactions.keyBy(Transaction::getAccountId).process(new KeyedProcessFunction<Long, Transaction, Object>() {

			private static final double SMALL_AMOUNT = 1.00;
			private static final double LARGE_AMOUNT = 500.00;
			private  transient ValueState<Boolean> flagState ;

			private   Logger LOG2 = LoggerFactory.getLogger(FraudDetectionJob_v1.class);

			@Override
			public void open(Configuration parameters) throws Exception {
				ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<Boolean>("flag", Types.BOOLEAN);
				flagState = getRuntimeContext().getState(flagDescriptor);
			}

			@Override
			public void processElement(Transaction value, Context ctx, Collector<Object> out) throws Exception {
				Boolean lastTransactionFlagState = flagState.value();

				//LOG.info("[详细信息] {}",value.toString());
				if(value.getAmount() < SMALL_AMOUNT){
					flagState.update(true);
				}

				if(flagState != null){
					if(value.getAmount() > LARGE_AMOUNT){
						out.collect(value);
					}
					flagState.clear();
				}



			}
		});


		dataStream2.addSink(new SinkFunction<Object>() {
			@Override
			public void invoke(Object value, Context context) throws Exception {
				LOG.info(value.toString());
			}
		});




		env.execute("Fraud Detection");

		System.out.println("============完成============");
	}
}
