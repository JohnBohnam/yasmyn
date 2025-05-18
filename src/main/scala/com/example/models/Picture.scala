package com.example.models

import java.sql.Timestamp


case class Picture(
                    id: Long,
                    userId: Long,
                    filename: String,
                    createdAt: Timestamp,
                    archived: Boolean
                  ) extends Serializable