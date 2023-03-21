package ai.quantumics.api.user.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "qs_aws_customer_info")
public class QsAwsCustomerInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int customerId;
	private int userId;
	private String customerIdentifier;
	private String productCode;
	private String dimension;
	private String entitlementValue;
	private Date expirationDate;
	private Date createdDate;
	private Date modifiedDate;
	private String awsAccountId;
}
