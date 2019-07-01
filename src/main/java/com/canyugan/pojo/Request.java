package com.canyugan.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Request 
{
	private Integer id;
	private String check_time;
	private Integer loading;
}
