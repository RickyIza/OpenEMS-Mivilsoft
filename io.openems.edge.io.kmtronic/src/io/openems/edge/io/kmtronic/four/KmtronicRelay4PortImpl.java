package io.openems.edge.io.kmtronic.four;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.CoilElement;
import io.openems.edge.bridge.modbus.api.task.FC1ReadCoilsTask;
import io.openems.edge.bridge.modbus.api.task.FC5WriteCoilTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.modbusslave.ModbusSlaveNatureTable;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.modbusslave.ModbusType;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.io.api.DigitalOutput;
import io.openems.edge.io.kmtronic.AbstractKmtronicRelay;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "IO.KMtronic.4Port", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class KmtronicRelay4PortImpl extends AbstractKmtronicRelay
		implements KmtronicRelay4Port, DigitalOutput, ModbusComponent, OpenemsComponent, ModbusSlave {

	@Reference
	protected ConfigurationAdmin cm;

	@Override
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public KmtronicRelay4PortImpl() {
		super(KmtronicRelay4Port.ChannelId.values());
	}

	@Activate
	void activate(ComponentContext context, Config config) throws OpenemsException {
		if (super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
		return new ModbusProtocol(this, //
				/*
				 * For Read: Read Coils
				 */
				new FC1ReadCoilsTask(0, Priority.LOW, //
						m(KmtronicRelay4Port.ChannelId.RELAY_1, new CoilElement(0)), //
						m(KmtronicRelay4Port.ChannelId.RELAY_2, new CoilElement(1)), //
						m(KmtronicRelay4Port.ChannelId.RELAY_3, new CoilElement(2)), //
						m(KmtronicRelay4Port.ChannelId.RELAY_4, new CoilElement(3)) //
				),
				/*
				 * For Write: Write Single Coil
				 */
				new FC5WriteCoilTask(0, m(KmtronicRelay4Port.ChannelId.RELAY_1, new CoilElement(0))), //
				new FC5WriteCoilTask(1, m(KmtronicRelay4Port.ChannelId.RELAY_2, new CoilElement(1))), //
				new FC5WriteCoilTask(2, m(KmtronicRelay4Port.ChannelId.RELAY_3, new CoilElement(2))), //
				new FC5WriteCoilTask(3, m(KmtronicRelay4Port.ChannelId.RELAY_4, new CoilElement(3))) //
		);
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		return new ModbusSlaveTable( //
				OpenemsComponent.getModbusSlaveNatureTable(accessMode), //
				ModbusSlaveNatureTable.of(KmtronicRelay4Port.class, accessMode, 100)//
						.channel(0, KmtronicRelay4Port.ChannelId.RELAY_1, ModbusType.UINT16) //
						.channel(1, KmtronicRelay4Port.ChannelId.RELAY_2, ModbusType.UINT16) //
						.channel(2, KmtronicRelay4Port.ChannelId.RELAY_3, ModbusType.UINT16) //
						.channel(3, KmtronicRelay4Port.ChannelId.RELAY_4, ModbusType.UINT16) //
						.build()//
		);
	}

}
